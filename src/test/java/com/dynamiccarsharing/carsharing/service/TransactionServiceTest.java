package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.exception.TransactionNotFoundException;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository);
    }

    private Transaction createTestTransaction(UUID id, TransactionStatus status) {
        return Transaction.builder()
                .id(id)
                .booking(Booking.builder().id(UUID.randomUUID()).build())
                .amount(new BigDecimal("125.50"))
                .status(status)
                .paymentMethod(PaymentType.CREDIT_CARD)
                .build();
    }

    @Test
    void save_shouldCallRepositoryAndReturnTransaction() {
        Transaction transactionToSave = createTestTransaction(null, TransactionStatus.PENDING);
        Transaction savedTransaction = createTestTransaction(UUID.randomUUID(), TransactionStatus.PENDING);
        when(transactionRepository.save(transactionToSave)).thenReturn(savedTransaction);

        Transaction result = transactionService.save(transactionToSave);

        assertNotNull(result);
        assertNotNull(result.getId());
        verify(transactionRepository).save(transactionToSave);
    }

    @Test
    void findById_whenTransactionExists_shouldReturnOptionalOfTransaction() {
        UUID transactionId = UUID.randomUUID();
        Transaction testTransaction = createTestTransaction(transactionId, TransactionStatus.COMPLETED);
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(testTransaction));

        Optional<Transaction> result = transactionService.findById(transactionId);

        assertTrue(result.isPresent());
        assertEquals(transactionId, result.get().getId());
    }

    @Test
    void findById_whenTransactionDoesNotExist_shouldReturnEmptyOptional() {
        UUID transactionId = UUID.randomUUID();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        Optional<Transaction> result = transactionService.findById(transactionId);

        assertFalse(result.isPresent());
    }

    @Test
    void deleteById_whenTransactionExists_shouldSucceed() {
        UUID transactionId = UUID.randomUUID();
        when(transactionRepository.existsById(transactionId)).thenReturn(true);
        doNothing().when(transactionRepository).deleteById(transactionId);

        transactionService.deleteById(transactionId);

        verify(transactionRepository).deleteById(transactionId);
    }

    @Test
    void deleteById_whenTransactionDoesNotExist_shouldThrowTransactionNotFoundException() {
        UUID transactionId = UUID.randomUUID();
        when(transactionRepository.existsById(transactionId)).thenReturn(false);

        assertThrows(TransactionNotFoundException.class, () -> {
            transactionService.deleteById(transactionId);
        });
    }

    @Test
    void findAll_shouldReturnListOfTransactions() {
        when(transactionRepository.findAll()).thenReturn(List.of(createTestTransaction(UUID.randomUUID(), TransactionStatus.PENDING)));

        List<Transaction> results = transactionService.findAll();

        assertEquals(1, results.size());
    }

    @Test
    void findTransactionsByStatus_shouldCallRepository() {
        TransactionStatus status = TransactionStatus.APPROVED;
        when(transactionRepository.findByStatus(status)).thenReturn(List.of(createTestTransaction(UUID.randomUUID(), status)));

        transactionService.findTransactionsByStatus(status);

        verify(transactionRepository).findByStatus(status);
    }

    @Test
    void findTransactionsByBookingId_shouldCallRepository() {
        UUID bookingId = UUID.randomUUID();
        when(transactionRepository.findByBookingId(bookingId)).thenReturn(List.of(createTestTransaction(UUID.randomUUID(), TransactionStatus.PENDING)));

        transactionService.findTransactionsByBookingId(bookingId);

        verify(transactionRepository).findByBookingId(bookingId);
    }

    @Test
    void searchTransactions_withCriteria_shouldCallRepositoryWithSpecification() {
        UUID bookingId = UUID.randomUUID();
        when(transactionRepository.findAll(any(Specification.class))).thenReturn(List.of(createTestTransaction(UUID.randomUUID(), TransactionStatus.PENDING)));

        List<Transaction> results = transactionService.searchTransactions(bookingId, null, null);

        assertFalse(results.isEmpty());
        verify(transactionRepository, times(1)).findAll(any(Specification.class));
    }
}