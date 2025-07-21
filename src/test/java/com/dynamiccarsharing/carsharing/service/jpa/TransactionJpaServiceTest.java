package com.dynamiccarsharing.carsharing.service.jpa;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.jpa.TransactionJpaRepository;
import com.dynamiccarsharing.carsharing.dto.TransactionSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceJpaTest {

    @Mock
    private TransactionJpaRepository transactionRepository;

    private TransactionServiceJpaImpl transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceJpaImpl(transactionRepository);
    }

    private Transaction createTestTransaction(Long id, TransactionStatus status) {
        return Transaction.builder()
                .id(id)
                .booking(Booking.builder().id(1L).build())
                .amount(new BigDecimal("125.50"))
                .status(status)
                .paymentMethod(PaymentType.CREDIT_CARD)
                .build();
    }

    @Test
    void findById_whenTransactionExists_shouldReturnOptionalOfTransaction() {
        Long transactionId = 1L;
        Transaction testTransaction = createTestTransaction(transactionId, TransactionStatus.COMPLETED);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));

        Optional<Transaction> result = transactionService.findById(transactionId);

        assertTrue(result.isPresent());
        assertEquals(transactionId, result.get().getId());
    }

    @Test
    void findById_whenTransactionDoesNotExist_shouldReturnEmptyOptional() {
        Long transactionId = 1L;
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        Optional<Transaction> result = transactionService.findById(transactionId);

        assertFalse(result.isPresent());
    }

    @Test
    void findAll_shouldReturnListOfTransactions() {
        when(transactionRepository.findAll()).thenReturn(List.of(createTestTransaction(1L, TransactionStatus.PENDING)));

        List<Transaction> results = transactionService.findAll();

        assertEquals(1, results.size());
    }

    @Test
    void findTransactionsByBookingId_shouldCallRepository() {
        Long bookingId = 1L;
        when(transactionRepository.findByBookingId(bookingId)).thenReturn(List.of(createTestTransaction(1L, TransactionStatus.PENDING)));

        transactionService.findTransactionsByBookingId(bookingId);

        verify(transactionRepository).findByBookingId(bookingId);
    }

    @Test
    void searchTransactions_withCriteria_shouldCallRepositoryWithSpecification() {
        Long bookingId = 1L;
        TransactionSearchCriteria criteria = TransactionSearchCriteria.builder().bookingId(bookingId).build();
        when(transactionRepository.findAll(any(Specification.class))).thenReturn(List.of(createTestTransaction(1L, TransactionStatus.PENDING)));

        List<Transaction> results = transactionService.searchTransactions(criteria);

        assertFalse(results.isEmpty());
        verify(transactionRepository, times(1)).findAll(any(Specification.class));
    }
}