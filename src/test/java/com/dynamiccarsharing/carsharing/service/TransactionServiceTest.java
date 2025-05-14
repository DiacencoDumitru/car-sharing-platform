package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    TransactionRepository transactionRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reset(transactionRepository);
        transactionService = new TransactionService(transactionRepository);
    }

    private Transaction createTestTransaction(TransactionStatus status) {
        return new Transaction(1L, 100.0, status != null ? status : TransactionStatus.PENDING, PaymentType.CREDIT_CARD, LocalDateTime.now(), null);
    }

    @Test
    void save_shouldCallRepository_shouldReturnSameTransaction() {
        Transaction transaction = createTestTransaction(TransactionStatus.PENDING);
        when(transactionRepository.save(transaction)).thenReturn(transaction);

        Transaction savedTransaction = transactionService.save(transaction);

        verify(transactionRepository, times(1)).save(transaction);
        assertSame(transaction, savedTransaction);
        assertEquals(transaction.getId(), savedTransaction.getId());
        assertEquals(transaction.getAmount(), savedTransaction.getAmount(), 0.001);
        assertEquals(transaction.getStatus(), savedTransaction.getStatus());
        assertEquals(transaction.getPaymentMethod(), savedTransaction.getPaymentMethod());
        assertEquals(transaction.getCreatedAt(), savedTransaction.getCreatedAt());
        assertEquals(transaction.getUpdatedAt(), savedTransaction.getUpdatedAt());
    }

    @Test
    void save_whenTransactionIsNull_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transactionService.save(null));

        assertEquals("Transaction must be non-null", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void findById_whenTransactionIsPresent_shouldReturnTransaction() {
        Transaction transaction = createTestTransaction(TransactionStatus.PENDING);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));

        Optional<Transaction> foundTransaction = transactionService.findById(1L);

        verify(transactionRepository, times(1)).findById(1L);
        assertTrue(foundTransaction.isPresent());
        assertSame(transaction, foundTransaction.get());
        assertEquals(transaction.getId(), foundTransaction.get().getId());
        assertEquals(transaction.getAmount(), foundTransaction.get().getAmount(), 0.001);
        assertEquals(TransactionStatus.PENDING, foundTransaction.get().getStatus());
    }

    @Test
    void findById_whenTransactionNotFound_shouldReturnEmpty() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Transaction> foundTransaction = transactionService.findById(1L);

        verify(transactionRepository, times(1)).findById(1L);
        assertFalse(foundTransaction.isPresent());
    }

    @Test
    void findById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transactionService.findById(-1L));

        assertEquals("Transaction ID must be non-null and non-negative", exception.getMessage());
        verify(transactionRepository, never()).findById(any());
    }

    @Test
    void deleteById_withValidId_shouldDeleteTransaction() {
        transactionService.deleteById(1L);

        verify(transactionRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> transactionService.deleteById(-1L));

        assertEquals("Transaction ID must be non-null and non-negative", exception.getMessage());
        verify(transactionRepository, never()).findById(any());
    }

    @Test
    void findAll_withMultipleTransactions_shouldReturnAllTransactions() {
        Transaction transaction1 = createTestTransaction(TransactionStatus.PENDING);
        Transaction transaction2 = new Transaction(
                2L, 200.0, TransactionStatus.APPROVED, PaymentType.PAYPAL,
                LocalDateTime.now().minusHours(1), null
        );
        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);
        when(transactionRepository.findAll()).thenReturn(transactions);

        Iterable<Transaction> result = transactionService.findAll();

        verify(transactionRepository, times(1)).findAll();
        List<Transaction> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(transaction1));
        assertTrue(resultList.contains(transaction2));
        assertEquals(transaction1.getId(), resultList.get(0).getId());
        assertEquals(transaction1.getAmount(), resultList.get(0).getAmount(), 0.001);
        assertEquals(transaction1.getStatus(), resultList.get(0).getStatus());
    }

    @Test
    void findAll_withSingleTransaction_shouldReturnSingleTransaction() {
        Transaction transaction = createTestTransaction(TransactionStatus.PENDING);
        List<Transaction> transactions = Collections.singletonList(transaction);
        when(transactionRepository.findAll()).thenReturn(transactions);

        Iterable<Transaction> result = transactionService.findAll();

        verify(transactionRepository, times(1)).findAll();
        List<Transaction> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(1, resultList.size());
        assertSame(transaction, resultList.get(0));
        assertEquals(transaction.getId(), resultList.get(0).getId());
        assertEquals(transaction.getAmount(), resultList.get(0).getAmount(), 0.001);
        assertEquals(transaction.getStatus(), resultList.get(0).getStatus());
    }

    @Test
    void findAll_withNoTransactions_shouldReturnEmptyIterable() {
        List<Transaction> transactions = Collections.emptyList();
        when(transactionRepository.findAll()).thenReturn(transactions);

        Iterable<Transaction> result = transactionService.findAll();

        verify(transactionRepository, times(1)).findAll();
        List<Transaction> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(0, resultList.size());
    }

    @Test
    void findTransactionsByStatus_withStatus_shouldReturnTransactions() {
        Transaction transaction = createTestTransaction(TransactionStatus.APPROVED);
        List<Transaction> payments = List.of(transaction);
        when(transactionRepository.findByFilter(argThat(filter -> filter != null && filter.test(transaction) && transaction.getStatus().equals(TransactionStatus.APPROVED)))).thenReturn(payments);

        List<Transaction> result = transactionService.findTransactionsByStatus(TransactionStatus.APPROVED);

        assertEquals(1, result.size());
        assertEquals(transaction, result.get(0));
        verify(transactionRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(transaction) && transaction.getStatus().equals(TransactionStatus.APPROVED)));
    }
}