package com.dynamiccarsharing.booking.reminder;

import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingReminderSentRepository;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.contracts.enums.BookingReminderType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EmbeddedKafka(
        partitions = 1,
        topics = {"booking.reminders"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@ActiveProfiles({"integration", "jpa"})
@TestPropertySource(properties = {
        "application.messaging.kafka.enabled=true",
        "application.reminders.enabled=true",
        "application.reminders.hours-before-start=24",
        "application.reminders.hours-before-end=2",
        "application.reminders.scan-window-minutes=15",
        "spring.task.scheduling.enabled=false",
        "eureka.client.enabled=false"
})
class BookingReminderSchedulerIntegrationTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingReminderSentRepository bookingReminderSentRepository;

    @Autowired
    private BookingReminderScheduler bookingReminderScheduler;

    @BeforeEach
    void clean() {
        bookingReminderSentRepository.deleteAll();
        bookingRepository.findAll().forEach(b -> bookingRepository.deleteById(b.getId()));
    }

    @Test
    @DisplayName("Планировщик создаёт запись о напоминании о начале для бронирования в окне")
    void dispatchDueReminders_startWindow_persistsSent() {
        LocalDateTime now = LocalDateTime.now();
        Booking booking = bookingRepository.save(Booking.builder()
                .renterId(501L)
                .carId(502L)
                .pickupLocationId(503L)
                .startTime(now.plusHours(24).plusMinutes(5))
                .endTime(now.plusHours(48))
                .status(TransactionStatus.APPROVED)
                .build());

        bookingReminderScheduler.dispatchDueReminders();

        assertThat(bookingReminderSentRepository.findByBookingIdAndReminderType(booking.getId(), BookingReminderType.START))
                .isPresent();
    }

    @Test
    @DisplayName("Планировщик создаёт запись о напоминании о конце для бронирования в окне")
    void dispatchDueReminders_endWindow_persistsSent() {
        LocalDateTime now = LocalDateTime.now();
        Booking booking = bookingRepository.save(Booking.builder()
                .renterId(601L)
                .carId(602L)
                .pickupLocationId(603L)
                .startTime(now.minusHours(1))
                .endTime(now.plusHours(2).plusMinutes(5))
                .status(TransactionStatus.APPROVED)
                .build());

        bookingReminderScheduler.dispatchDueReminders();

        assertThat(bookingReminderSentRepository.findByBookingIdAndReminderType(booking.getId(), BookingReminderType.END))
                .isPresent();
    }
}
