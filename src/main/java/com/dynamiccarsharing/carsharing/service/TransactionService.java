package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.InMemoryTransactionRepository;
import com.dynamiccarsharing.carsharing.repository.filter.TransactionFilter;
import com.dynamiccarsharing.carsharing.util.Validator;

import java.util.List;
import java.util.Optional;

public class TransactionService {
    private final InMemoryTransactionRepository inMemoryTransactionRepository;

    public TransactionService(InMemoryTransactionRepository inMemoryTransactionRepository) {
        this.inMemoryTransactionRepository = inMemoryTransactionRepository;
    }

    public Transaction save(Transaction transaction) {
        Validator.validateNonNull(transaction, "Transaction");
        return inMemoryTransactionRepository.save(transaction);
    }

    public Optional<Transaction> findById(Long id) {
        Validator.validateId(id, "ID");
        return inMemoryTransactionRepository.findById(id);
    }

    public void delete(Long id) {
        Validator.validateId(id, "ID");
        inMemoryTransactionRepository.deleteById(id);
    }

    public Iterable<Transaction> findAll() {
        return inMemoryTransactionRepository.findAll();
    }

    public List<Transaction> findTransactionsByStatus(TransactionStatus status) {
        Validator.validateNonNull(status, "Transaction Status");
        TransactionFilter filter = new TransactionFilter().setStatus(status);
        return (List<Transaction>) inMemoryTransactionRepository.findByFilter(filter);
    }
}