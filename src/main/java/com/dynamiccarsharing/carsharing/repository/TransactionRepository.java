package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findByBookingId(UUID bookingId);
}
