package com.dynamiccarsharing.booking.repository.inmemory;

import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.model.Transaction;
import com.dynamiccarsharing.booking.repository.TransactionRepository;
import com.dynamiccarsharing.util.filter.Filter;

import java.util.*;

public class InMemoryTransactionRepositoryJdbcImpl implements TransactionRepository {
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
    public List<Transaction> findByFilter(Filter<Transaction> filter) {
        return transactionMap.values().stream().filter(filter::test).toList();
    }

    @Override
    public List<Transaction> findAll() {
        return new ArrayList<>(transactionMap.values());
    }

    @Override
    public List<Transaction> findByStatus(TransactionStatus status) {
        return transactionMap.values().stream()
                .filter(transaction -> transaction.getStatus() == status)
                .toList();
    }

    @Override
    public List<Transaction> findByBookingId(Long bookingId) {
        return transactionMap.values().stream()
                .filter(transaction -> transaction.getBooking() != null && transaction.getBooking().getId().equals(bookingId))
                .toList();
    }
}