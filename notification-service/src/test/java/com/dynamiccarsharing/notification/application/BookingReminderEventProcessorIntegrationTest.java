package com.dynamiccarsharing.notification.application;

import com.dynamiccarsharing.contracts.dto.BookingReminderEventDto;
import com.dynamiccarsharing.contracts.enums.BookingReminderType;
import com.dynamiccarsharing.notification.contacts.UserContactService;
import com.dynamiccarsharing.notification.reminder.BookingReminderDispatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

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
class BookingReminderEventProcessorIntegrationTest {

    @MockBean
    private UserContactService userContactService;

    @Autowired
    private BookingReminderEventProcessor processor;

    @Autowired
    private BookingReminderDispatchRepository dispatchRepository;

    @BeforeEach
    void clean() {
        dispatchRepository.deleteAll();
    }

    @Test
    @DisplayName("Напоминание сохраняется в журнале и не дублируется")
    void process_reminder_persistsDispatchOnce() {
        Mockito.when(userContactService.getRenterEmail(2L)).thenReturn(Optional.of("r@example.com"));
        Mockito.when(userContactService.getRenterPhoneNumber(2L)).thenReturn(Optional.of("+100"));

        BookingReminderEventDto event = BookingReminderEventDto.builder()
                .bookingId(30L)
                .renterId(2L)
                .carId(40L)
                .reminderType(BookingReminderType.START)
                .occurredAt(Instant.now())
                .build();

        processor.process(event);
        processor.process(event);

        assertThat(dispatchRepository.count()).isEqualTo(1);
    }
}
