package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.TransactionDto;
import com.dynamiccarsharing.carsharing.dto.criteria.TransactionSearchCriteria;
import com.dynamiccarsharing.carsharing.exception.ServiceException;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.TransactionFilter;
import com.dynamiccarsharing.carsharing.mapper.TransactionMapper;
import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.TransactionRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("transactionService")
@Transactional
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<TransactionDto> findTransactionById(Long id) {
        return transactionRepository.findById(id).map(transactionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> findAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(transactionMapper::toDto)
                .toList();
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
                criteria.getBookingId(),
                criteria.getStatus(),
                criteria.getPaymentMethod()
        );
        try {
            return transactionRepository.findByFilter(filter);
        } catch (SQLException e) {
            throw new ServiceException("Search for transactions failed", e);
        }
    }
}