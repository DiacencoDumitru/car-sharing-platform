package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.dto.TransactionDto;
import com.dynamiccarsharing.carsharing.dto.criteria.TransactionSearchCriteria;
import com.dynamiccarsharing.carsharing.model.Transaction;
<<<<<<< HEAD
=======
import com.dynamiccarsharing.carsharing.dto.criteria.TransactionSearchCriteria;
>>>>>>> fix/controller-mvc-tests

import java.util.List;
import java.util.Optional;

public interface TransactionService {
    Optional<TransactionDto> findTransactionById(Long id);

    List<TransactionDto> findAllTransactions();

    List<Transaction> findTransactionsByBookingId(Long bookingId);

    List<Transaction> searchTransactions(TransactionSearchCriteria criteria);
}