package com.dynamiccarsharing.booking.repository;

import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.model.Transaction;
import com.dynamiccarsharing.util.filter.Filter;
import com.dynamiccarsharing.util.repository.Repository;

import java.sql.SQLException;
import java.util.List;

public interface TransactionRepository extends Repository<Transaction, Long> {

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findByBookingId(Long bookingId);

    List<Transaction> findByFilter(Filter<Transaction> filter) throws SQLException;

    @Override
    List<Transaction> findAll();
}