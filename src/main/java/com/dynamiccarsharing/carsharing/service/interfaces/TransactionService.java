package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.dto.criteria.TransactionSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface TransactionService {
    Optional<Transaction> findById(Long id);

    List<Transaction> findAll();

    List<Transaction> findTransactionsByBookingId(Long bookingId);

    List<Transaction> searchTransactions(TransactionSearchCriteria criteria);
}