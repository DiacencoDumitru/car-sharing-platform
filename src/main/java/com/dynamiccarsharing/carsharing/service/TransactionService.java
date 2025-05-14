package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.TransactionRepository;
import com.dynamiccarsharing.carsharing.repository.filter.TransactionFilter;
import com.dynamiccarsharing.carsharing.util.Validator;

import java.util.List;
import java.util.Optional;

public class TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction save(Transaction transaction) {
        Validator.validateNonNull(transaction, "Transaction");
        return transactionRepository.save(transaction);
    }

    public Optional<Transaction> findById(Long id) {
        Validator.validateId(id, "Transaction ID");
        return transactionRepository.findById(id);
    }

    public void deleteById(Long id) {
        Validator.validateId(id, "Transaction ID");
        transactionRepository.deleteById(id);
    }

    public Iterable<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public List<Transaction> findTransactionsByStatus(TransactionStatus status) {
        Validator.validateNonNull(status, "Transaction Status");
        TransactionFilter filter = new TransactionFilter().setStatus(status);
        return transactionRepository.findByFilter(filter);
    }
}