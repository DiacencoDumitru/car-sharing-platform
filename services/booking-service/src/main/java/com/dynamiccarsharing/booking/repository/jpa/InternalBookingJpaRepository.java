package com.dynamiccarsharing.booking.repository.jpa;

import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Profile("jpa")
interface InternalBookingJpaRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    List<Booking> findByRenterId(Long renterId);

    @Query("SELECT b FROM Booking b WHERE b.carId = :carId AND b.status IN :statuses AND b.startTime < :endTime AND b.endTime > :startTime")
    List<Booking> findOverlapping(@Param("carId") Long carId, @Param("statuses") List<TransactionStatus> statuses, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT b FROM Booking b WHERE b.status = :approved "
            + "AND b.startTime > :now AND b.startTime > :startLower AND b.startTime <= :startUpper")
    List<Booking> findStartReminderCandidates(
            @Param("approved") TransactionStatus approved,
            @Param("now") LocalDateTime now,
            @Param("startLower") LocalDateTime startLower,
            @Param("startUpper") LocalDateTime startUpper);

    @Query("SELECT b FROM Booking b WHERE b.status = :approved "
            + "AND b.endTime > :now AND b.endTime > :endLower AND b.endTime <= :endUpper")
    List<Booking> findEndReminderCandidates(
            @Param("approved") TransactionStatus approved,
            @Param("now") LocalDateTime now,
            @Param("endLower") LocalDateTime endLower,
            @Param("endUpper") LocalDateTime endUpper);
}