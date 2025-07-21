package com.dynamiccarsharing.carsharing.service.jdbc;

import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.TransactionFilter;
import com.dynamiccarsharing.carsharing.repository.jdbc.TransactionRepositoryJdbcImpl;
import com.dynamiccarsharing.carsharing.service.interfaces.TransactionService;
import com.dynamiccarsharing.carsharing.dto.TransactionSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service("transactionService")
@Profile("jdbc")
@Transactional
public class TransactionServiceJdbcImpl implements TransactionService {

    private final TransactionRepositoryJdbcImpl transactionRepositoryJdbcImpl;

    public TransactionServiceJdbcImpl(TransactionRepositoryJdbcImpl transactionRepositoryJdbcImpl) {
        this.transactionRepositoryJdbcImpl = transactionRepositoryJdbcImpl;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Transaction> findById(Long id) {
        return transactionRepositoryJdbcImpl.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> findAll() {
        List<Transaction> transactionList = new ArrayList<>();
        transactionRepositoryJdbcImpl.findAll().forEach(transactionList::add);
        return transactionList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> findTransactionsByBookingId(Long bookingId) {
        return transactionRepositoryJdbcImpl.findByBookingId(bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> searchTransactions(TransactionSearchCriteria criteria) {
        Filter<Transaction> filter = createFilterFromCriteria(criteria);
        try {
            return transactionRepositoryJdbcImpl.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search for transactions failed", e);
        }
    }

    private Filter<Transaction> createFilterFromCriteria(TransactionSearchCriteria criteria) {
        return TransactionFilter.of(
                null,
                criteria.getBookingId(),
                criteria.getStatus(),
                criteria.getPaymentMethod()
        );
    }
}