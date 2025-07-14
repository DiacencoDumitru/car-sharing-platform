package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.exception.TransactionNotFoundException;
import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.TransactionRepository;
import com.dynamiccarsharing.carsharing.repository.specification.TransactionSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public Optional<Transaction> findById(UUID id) {
        return transactionRepository.findById(id);
    }

    public void deleteById(UUID id) {
        if (!transactionRepository.existsById(id)) {
            throw new TransactionNotFoundException("Transaction with ID " + id + " not found.");
        }
        transactionRepository.deleteById(id);
    }

    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public List<Transaction> findTransactionsByStatus(TransactionStatus status) {
        return transactionRepository.findByStatus(status);
    }

    public List<Transaction> findTransactionsByBookingId(UUID bookingId) {
        return transactionRepository.findByBookingId(bookingId);
    }

    public List<Transaction> searchTransactions(UUID bookingId, TransactionStatus status, PaymentType paymentMethod) {
        Specification<Transaction> spec = Specification
                .where(bookingId != null ? TransactionSpecification.hasBookingId(bookingId) : null)
                .and(status != null ? TransactionSpecification.hasStatus(status) : null)
                .and(paymentMethod != null ? TransactionSpecification.hasPaymentMethod(paymentMethod) : null);

        return transactionRepository.findAll(spec);
    }
}