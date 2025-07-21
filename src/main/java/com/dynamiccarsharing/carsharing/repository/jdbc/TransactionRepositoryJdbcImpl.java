package com.dynamiccarsharing.carsharing.repository.jdbc;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Transaction;

import java.util.List;

public interface TransactionRepositoryJdbcImpl extends Repository<Transaction, Long> {
    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findByBookingId(Long bookingId);
}
