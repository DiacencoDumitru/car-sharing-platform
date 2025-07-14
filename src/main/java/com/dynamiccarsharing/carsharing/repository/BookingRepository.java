package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {
    @EntityGraph(attributePaths = {"transactions"})
    Optional<Booking> findWithTransactionById(UUID id);

    List<Booking> findByRenterId(UUID renterId);

    List<Booking> findByCarId(UUID carId);

    List<Booking> findByStatus(TransactionStatus status);
}
