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
}