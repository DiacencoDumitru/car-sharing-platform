package com.dynamiccarsharing.booking.repository.jpa;

import com.dynamiccarsharing.booking.model.BookingWaitlistEntry;
import com.dynamiccarsharing.booking.model.BookingWaitlistStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("jpa")
interface InternalBookingWaitlistJpaRepository extends JpaRepository<BookingWaitlistEntry, Long> {
    @Query("SELECT w FROM BookingWaitlistEntry w WHERE w.id = :id AND w.status = 'ACTIVE'")
    Optional<BookingWaitlistEntry> findActiveById(@Param("id") Long id);

    @Query("SELECT w FROM BookingWaitlistEntry w WHERE w.renterId = :renterId AND w.carId = :carId "
            + "AND w.startTime = :startTime AND w.endTime = :endTime AND w.status = 'ACTIVE'")
    Optional<BookingWaitlistEntry> findActiveDuplicate(
            @Param("renterId") Long renterId,
            @Param("carId") Long carId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT w FROM BookingWaitlistEntry w WHERE w.carId = :carId AND w.status = :status "
            + "AND w.startTime < :endTime AND w.endTime > :startTime ORDER BY w.createdAt ASC")
    List<BookingWaitlistEntry> findOverlappingByCarAndStatus(
            @Param("carId") Long carId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("status") BookingWaitlistStatus status);
}
