package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.criteria.TransactionSearchCriteria;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.TransactionFilter;
import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.TransactionRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("transactionService")
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Transaction> findById(Long id) {
        return transactionRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> findTransactionsByBookingId(Long bookingId) {
        return transactionRepository.findByBookingId(bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> searchTransactions(TransactionSearchCriteria criteria) {
        Filter<Transaction> filter = TransactionFilter.of(
                null,
                criteria.getBookingId(),
                criteria.getStatus(),
                criteria.getPaymentMethod()
        );
        try {
            return transactionRepository.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search for transactions failed", e);
        }
    }
}