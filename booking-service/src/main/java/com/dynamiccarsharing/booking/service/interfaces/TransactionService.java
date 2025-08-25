package com.dynamiccarsharing.booking.service.interfaces;

import com.dynamiccarsharing.booking.criteria.TransactionSearchCriteria;
import com.dynamiccarsharing.booking.dto.TransactionDto;

import java.util.List;
import java.util.Optional;

public interface TransactionService {
    Optional<TransactionDto> findTransactionById(Long id);

    List<TransactionDto> findAllTransactions();

    List<TransactionDto> findTransactionsByBookingId(Long bookingId);

    List<TransactionDto> searchTransactions(TransactionSearchCriteria criteria);
}