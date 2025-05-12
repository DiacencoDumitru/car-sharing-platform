package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTransactionRepository implements TransactionRepository {
    private final Map<Long, Transaction> transactionMap = new HashMap<>();

    @Override
    public Transaction save(Transaction transaction) {
        transactionMap.put(transaction.getId(), transaction);
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return Optional.ofNullable(transactionMap.get(id));
    }

    @Override
    public void deleteById(Long id) {
        transactionMap.remove(id);
    }

    @Override
    public Iterable<Transaction> findAll() {
        return transactionMap.values();
    }

    public Iterable<Transaction> findByFilter(Filter<Transaction> filter) {
        return transactionMap.values().stream().filter(filter::test).collect(Collectors.toList());
    }
}
