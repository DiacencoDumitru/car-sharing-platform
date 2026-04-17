package com.dynamiccarsharing.booking.reminder;

import com.dynamiccarsharing.booking.messaging.kafka.BookingReminderPublisher;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.BookingReminderSent;
import com.dynamiccarsharing.booking.repository.BookingReminderSentRepository;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.contracts.dto.BookingReminderEventDto;
import com.dynamiccarsharing.contracts.enums.BookingReminderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("jpa")
@ConditionalOnProperty(
        name = {"application.reminders.enabled", "application.messaging.kafka.enabled"},
        havingValue = "true",
        matchIfMissing = false
)
public class BookingReminderScheduler {

    private final BookingRepository bookingRepository;
    private final BookingReminderSentRepository bookingReminderSentRepository;
    private final BookingReminderPublisher bookingReminderPublisher;

    @Value("${application.reminders.hours-before-start:24}")
    private int hoursBeforeStart;

    @Value("${application.reminders.hours-before-end:2}")
    private int hoursBeforeEnd;

    @Value("${application.reminders.scan-window-minutes:15}")
    private int scanWindowMinutes;

    @Scheduled(fixedDelayString = "${application.reminders.scan-interval-ms:900000}")
    @Transactional
    public void dispatchDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startLower = now.plusHours(hoursBeforeStart);
        LocalDateTime startUpper = startLower.plusMinutes(scanWindowMinutes);
        List<Booking> startCandidates = bookingRepository.findStartReminderCandidates(now, startLower, startUpper);
        java.util.Set<Long> seenStart = new java.util.HashSet<>();
        for (Booking b : startCandidates) {
            if (seenStart.add(b.getId())) {
                sendIfNew(b, BookingReminderType.START);
            }
        }

        LocalDateTime endLower = now.plusHours(hoursBeforeEnd);
        LocalDateTime endUpper = endLower.plusMinutes(scanWindowMinutes);
        List<Booking> endCandidates = bookingRepository.findEndReminderCandidates(now, endLower, endUpper);
        java.util.Set<Long> seenEnd = new java.util.HashSet<>();
        for (Booking b : endCandidates) {
            if (seenEnd.add(b.getId())) {
                sendIfNew(b, BookingReminderType.END);
            }
        }
    }

    private void sendIfNew(Booking booking, BookingReminderType type) {
        if (bookingReminderSentRepository.findByBookingIdAndReminderType(booking.getId(), type).isPresent()) {
            return;
        }
        BookingReminderEventDto event = BookingReminderEventDto.builder()
                .bookingId(booking.getId())
                .renterId(booking.getRenterId())
                .carId(booking.getCarId())
                .reminderType(type)
                .occurredAt(Instant.now())
                .build();
        bookingReminderPublisher.publish(event);
        bookingReminderSentRepository.save(BookingReminderSent.builder()
                .bookingId(booking.getId())
                .reminderType(type)
                .build());
    }
}
