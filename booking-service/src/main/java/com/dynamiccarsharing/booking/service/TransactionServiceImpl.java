package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.criteria.TransactionSearchCriteria;
import com.dynamiccarsharing.booking.dto.TransactionDto;
import com.dynamiccarsharing.booking.filter.TransactionFilter;
import com.dynamiccarsharing.booking.mapper.TransactionMapper;
import com.dynamiccarsharing.booking.model.Transaction;
import com.dynamiccarsharing.booking.repository.TransactionRepository;
import com.dynamiccarsharing.booking.service.interfaces.TransactionService;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.filter.Filter;
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
    public List<TransactionDto> findTransactionsByBookingId(Long bookingId) {
        return transactionRepository.findByBookingId(bookingId).stream().map(transactionMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> searchTransactions(TransactionSearchCriteria criteria) {
        Filter<Transaction> filter = TransactionFilter.of(
                criteria.getBookingId(),
                criteria.getStatus(),
                criteria.getPaymentMethod()
        );
        try {
            return transactionRepository.findByFilter(filter).stream().map(transactionMapper::toDto).toList();
        } catch (SQLException e) {
            throw new ServiceException("Search for transactions failed", e);
        }
    }
}