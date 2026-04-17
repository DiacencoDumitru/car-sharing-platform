package com.dynamiccarsharing.booking.repository;

import com.dynamiccarsharing.booking.model.BookingReminderSent;
import com.dynamiccarsharing.contracts.enums.BookingReminderType;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("jpa")
public interface BookingReminderSentRepository extends JpaRepository<BookingReminderSent, Long> {

    Optional<BookingReminderSent> findByBookingIdAndReminderType(Long bookingId, BookingReminderType reminderType);
}
