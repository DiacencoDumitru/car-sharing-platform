package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.Transaction;

import java.sql.SQLException;
import java.util.List;

public interface TransactionRepository extends Repository<Transaction, Long> {

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findByBookingId(Long bookingId);

    List<Transaction> findByFilter(Filter<Transaction> filter) throws SQLException;

    @Override
    List<Transaction> findAll();
}