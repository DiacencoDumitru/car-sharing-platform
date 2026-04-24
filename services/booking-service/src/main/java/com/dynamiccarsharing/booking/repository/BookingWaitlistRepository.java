package com.dynamiccarsharing.booking.repository;

import com.dynamiccarsharing.booking.model.BookingWaitlistEntry;
import com.dynamiccarsharing.booking.model.BookingWaitlistStatus;
import com.dynamiccarsharing.util.repository.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingWaitlistRepository extends Repository<BookingWaitlistEntry, Long> {
    Optional<BookingWaitlistEntry> findActiveById(Long id);

    Optional<BookingWaitlistEntry> findActiveDuplicate(Long renterId, Long carId, LocalDateTime startTime, LocalDateTime endTime);

    List<BookingWaitlistEntry> findOverlappingByCarAndStatus(Long carId, LocalDateTime startTime, LocalDateTime endTime, BookingWaitlistStatus status);
}
