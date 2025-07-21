package com.dynamiccarsharing.carsharing.service.jpa;

import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.jpa.TransactionJpaRepository;
import com.dynamiccarsharing.carsharing.specification.TransactionSpecification;
import com.dynamiccarsharing.carsharing.service.interfaces.TransactionService;
import com.dynamiccarsharing.carsharing.dto.TransactionSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("transactionService")
@Profile("jpa")
@Transactional
public class TransactionServiceJpaImpl implements TransactionService {

    private final TransactionJpaRepository transactionRepository;

    public TransactionServiceJpaImpl(TransactionJpaRepository transactionRepository) {
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
        return transactionRepository.findAll(
                TransactionSpecification.withCriteria(
                        criteria.getBookingId(),
                        criteria.getStatus(),
                        criteria.getPaymentMethod()
                )
        );
    }
}