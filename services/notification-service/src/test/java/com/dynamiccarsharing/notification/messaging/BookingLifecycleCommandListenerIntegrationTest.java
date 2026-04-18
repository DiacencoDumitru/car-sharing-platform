package com.dynamiccarsharing.notification.messaging;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.notification.analytics.BookingLifecycleAnalyticsEvent;
import com.dynamiccarsharing.notification.analytics.BookingLifecycleAnalyticsEventRepository;
import com.dynamiccarsharing.notification.application.BookingLifecycleEventProcessor;
import com.dynamiccarsharing.notification.contacts.UserContactService;
import com.dynamiccarsharing.notification.fraud.SimpleAntiFraudService;
import com.dynamiccarsharing.notification.notify.NotificationMessage;
import com.dynamiccarsharing.notification.notify.NotificationMessageRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "application.messaging.kafka.enabled=false",
                "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                "spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                "spring.kafka.consumer.properties.spring.json.trusted.packages="
        }
)
@ActiveProfiles("integration")
class BookingLifecycleCommandListenerIntegrationTest {

    @MockBean
    private UserContactService userContactService;

    @Autowired
    private BookingLifecycleEventProcessor processor;

    @Autowired
    private BookingLifecycleAnalyticsEventRepository repository;

    @Autowired
    private NotificationMessageRepository notificationMessageRepository;

    @Autowired
    private SimpleAntiFraudService simpleAntiFraudService;

    @Test
    void bookingApproved_eventIsProcessed_andAnalyticsRecordIsWritten() {
        repository.deleteAll();
        notificationMessageRepository.deleteAll();

        Mockito.when(userContactService.getRenterEmail(2L)).thenReturn(Optional.of("renter2@example.com"));
        Mockito.when(userContactService.getRenterPhoneNumber(2L)).thenReturn(Optional.of("+10000000002"));

        BookingLifecycleEventDto event = BookingLifecycleEventDto.builder()
                .bookingId(1L)
                .renterId(2L)
                .carId(3L)
                .bookingStatus(TransactionStatus.APPROVED)
                .occurredAt(Instant.now())
                .build();

        processor.process(event);

        BookingLifecycleAnalyticsEvent saved = repository.findByBookingIdAndBookingStatus(event.getBookingId(), event.getBookingStatus())
                .orElseThrow();
        assertThat(saved.getFraudRiskScore()).isEqualTo(80);
        assertThat(saved.isAttentionRequired()).isTrue();
        assertThat(saved.isNotificationSent()).isTrue();

        assertThat(notificationMessageRepository.count()).isEqualTo(2);

        NotificationMessage emailMsg = notificationMessageRepository.findAll().stream()
                .filter(m -> "EMAIL".equals(m.getChannel()))
                .findFirst()
                .orElseThrow();
        NotificationMessage pushMsg = notificationMessageRepository.findAll().stream()
                .filter(m -> "PUSH".equals(m.getChannel()))
                .findFirst()
                .orElseThrow();

        assertThat(emailMsg.getRecipient()).isEqualTo("renter2@example.com");
        assertThat(emailMsg.getBookingId()).isEqualTo(1L);
        assertThat(emailMsg.getBookingStatus()).isEqualTo(TransactionStatus.APPROVED);

        assertThat(pushMsg.getRecipient()).isEqualTo("+10000000002");
        assertThat(pushMsg.getBookingId()).isEqualTo(1L);
        assertThat(pushMsg.getBookingStatus()).isEqualTo(TransactionStatus.APPROVED);
    }

    @Test
    void sendingSameEventTwice_doesNotCreateDuplicateAnalyticsRecords() {
        repository.deleteAll();
        notificationMessageRepository.deleteAll();

        Mockito.when(userContactService.getRenterEmail(4L)).thenReturn(Optional.of("renter4@example.com"));
        Mockito.when(userContactService.getRenterPhoneNumber(4L)).thenReturn(Optional.of("+10000000004"));

        BookingLifecycleEventDto event = BookingLifecycleEventDto.builder()
                .bookingId(11L)
                .renterId(4L)
                .carId(5L)
                .bookingStatus(TransactionStatus.APPROVED)
                .occurredAt(Instant.now())
                .build();

        processor.process(event);
        processor.process(event);

        assertThat(repository.count()).isEqualTo(1);
        assertThat(notificationMessageRepository.count()).isEqualTo(2);
    }

    @Test
    void approvedThenCompletedWithinWindow_escalatesFraudRisk_andSendsNotifications() {
        repository.deleteAll();
        notificationMessageRepository.deleteAll();

        Long bookingId = 21L;
        Long renterId = 6L;

        Instant approvedAt = Instant.now();

        Mockito.when(userContactService.getRenterEmail(renterId)).thenReturn(Optional.of("renter6@example.com"));
        Mockito.when(userContactService.getRenterPhoneNumber(renterId)).thenReturn(Optional.of("+10000000006"));

        long approveCancelWindowSeconds = readApproveCancelWindowSeconds();

        BookingLifecycleEventDto approved = BookingLifecycleEventDto.builder()
                .bookingId(bookingId)
                .renterId(renterId)
                .carId(7L)
                .bookingStatus(TransactionStatus.APPROVED)
                .occurredAt(approvedAt)
                .build();

        processor.process(approved);

        var approvedAnalyticsOpt = repository.findByBookingIdAndBookingStatus(bookingId, TransactionStatus.APPROVED);

        assertThat(approvedAnalyticsOpt).isPresent();
        assertThat(approvedAnalyticsOpt.get().getOccurredAt()).isNotNull();

        long safeWithinSeconds = Math.max(0, approveCancelWindowSeconds - 10);
        Instant completedAt = approvedAnalyticsOpt.get().getOccurredAt().plusSeconds(safeWithinSeconds);

        BookingLifecycleEventDto completed = BookingLifecycleEventDto.builder()
                .bookingId(bookingId)
                .renterId(renterId)
                .carId(7L)
                .bookingStatus(TransactionStatus.COMPLETED)
                .occurredAt(completedAt)
                .build();

        processor.process(completed);

        BookingLifecycleAnalyticsEvent completedSaved = repository.findByBookingIdAndBookingStatus(bookingId, TransactionStatus.COMPLETED)
                .orElseThrow();
        assertThat(completedSaved).isNotNull();
        assertThat(completedSaved.getFraudRiskScore()).isEqualTo(65);
        assertThat(completedSaved.isAttentionRequired()).isTrue();
        assertThat(completedSaved.isNotificationSent()).isTrue();

        assertThat(notificationMessageRepository.count()).isEqualTo(4);
    }

    private long readApproveCancelWindowSeconds() {
        try {
            Field field = simpleAntiFraudService.getClass().getDeclaredField("approveCancelWindowSeconds");
            field.setAccessible(true);
            return (long) field.get(simpleAntiFraudService);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read approveCancelWindowSeconds from SimpleAntiFraudService", e);
        }
    }
}
