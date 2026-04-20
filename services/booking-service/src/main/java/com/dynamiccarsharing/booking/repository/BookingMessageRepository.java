package com.dynamiccarsharing.booking.repository;

import com.dynamiccarsharing.booking.model.BookingMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingMessageRepository extends JpaRepository<BookingMessage, Long> {

    List<BookingMessage> findByBooking_IdAndIdGreaterThanOrderByIdAsc(Long bookingId, Long afterId, Pageable pageable);
}
