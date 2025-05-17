package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.filter.TransactionFilter;

import java.util.List;

public interface TransactionRepository extends Repository<Transaction, Long> {
    List<Transaction> findByFilter(TransactionFilter filter);
}
