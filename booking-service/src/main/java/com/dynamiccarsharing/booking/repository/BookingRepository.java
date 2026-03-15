package com.dynamiccarsharing.booking.repository;

import com.dynamiccarsharing.booking.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.util.repository.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends Repository<Booking, Long> {

    List<Booking> findByRenterId(Long renterId);

    Page<Booking> findAll(BookingSearchCriteria criteria, Pageable pageable);

    boolean hasOverlappingBooking(Long carId, LocalDateTime startTime, LocalDateTime endTime);
}