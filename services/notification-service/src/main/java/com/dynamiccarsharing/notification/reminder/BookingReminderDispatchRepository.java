package com.dynamiccarsharing.notification.reminder;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingReminderDispatchRepository extends JpaRepository<BookingReminderDispatch, Long> {

    Optional<BookingReminderDispatch> findByBookingIdAndReminderType(Long bookingId, String reminderType);
}
