package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.filter.TransactionFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryTransactionRepositoryTest {

    private InMemoryTransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransactionRepository();
        repository.findAll().forEach(transaction -> repository.deleteById(transaction.getId()));
    }

    private Transaction createTestTransaction(Long id, TransactionStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return new Transaction(id, 100.0, status, PaymentType.CREDIT_CARD, now, status == TransactionStatus.COMPLETED ? now : null);
    }

    @Test
    void save_shouldSaveAndReturnTransaction() {
        Transaction transaction = createTestTransaction(1L, TransactionStatus.PENDING);

        Transaction savedTransaction = repository.save(transaction);

        assertEquals(transaction, savedTransaction);
        assertTrue(repository.findById(1L).isPresent());
        assertEquals(transaction, repository.findById(1L).get());
    }

    @Test
    void save_withNullTransaction_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
    }

    @Test
    void findById_withExistingId_shouldReturnTransaction() {
        Transaction transaction = createTestTransaction(1L, TransactionStatus.PENDING);
        repository.save(transaction);

        Optional<Transaction> foundTransaction = repository.findById(1L);

        assertTrue(foundTransaction.isPresent());
        assertEquals(transaction, foundTransaction.get());
    }

    @Test
    void findById_withNonExistingId_shouldReturnEmpty() {
        Optional<Transaction> foundTransaction = repository.findById(1L);

        assertFalse(foundTransaction.isPresent());
    }

    @Test
    void deleteById_withExistingId_shouldRemoveTransaction() {
        Transaction transaction = createTestTransaction(1L, TransactionStatus.PENDING);
        repository.save(transaction);

        repository.deleteById(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void deleteById_withNonExistingId_shouldDoNothing() {
        repository.deleteById(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void findAll_withMultipleTransactions_shouldReturnAllTransactions() {
        Transaction transaction1 = createTestTransaction(1L, TransactionStatus.PENDING);
        Transaction transaction2 = createTestTransaction(2L, TransactionStatus.COMPLETED);
        repository.save(transaction1);
        repository.save(transaction2);

        Iterable<Transaction> transactions = repository.findAll();
        List<Transaction> transactionList = new ArrayList<>();
        transactions.forEach(transactionList::add);

        assertEquals(2, transactionList.size());
        assertTrue(transactionList.contains(transaction1));
        assertTrue(transactionList.contains(transaction2));
    }

    @Test
    void findAll_withSingleTransaction_shouldReturnSingleTransaction() {
        Transaction transaction = createTestTransaction(1L, TransactionStatus.PENDING);
        repository.save(transaction);

        Iterable<Transaction> transactions = repository.findAll();
        List<Transaction> transactionList = new ArrayList<>();
        transactions.forEach(transactionList::add);

        assertEquals(1, transactionList.size());
        assertEquals(transaction, transactionList.get(0));
    }

    @Test
    void findAll_withNoTransactions_shouldReturnEmptyIterable() {
        Iterable<Transaction> transactions = repository.findAll();
        List<Transaction> transactionList = new ArrayList<>();
        transactions.forEach(transactionList::add);

        assertEquals(0, transactionList.size());
    }

    @Test
    void findByFilter_withMatchingTransactions_shouldReturnMatchingTransactions() {
        Transaction transaction1 = createTestTransaction(1L, TransactionStatus.PENDING);
        Transaction transaction2 = createTestTransaction(2L, TransactionStatus.COMPLETED);
        Transaction transaction3 = createTestTransaction(3L, TransactionStatus.PENDING);
        repository.save(transaction1);
        repository.save(transaction2);
        repository.save(transaction3);
        TransactionFilter filter = mock(TransactionFilter.class);
        when(filter.test(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            return transaction.getStatus() == TransactionStatus.PENDING;
        });

        List<Transaction> filteredTransactions = repository.findByFilter(filter);

        assertEquals(2, filteredTransactions.size());
        assertTrue(filteredTransactions.contains(transaction1));
        assertTrue(filteredTransactions.contains(transaction3));
        assertFalse(filteredTransactions.contains(transaction2));
    }

    @Test
    void findByFilter_withNoMatchingTransactions_shouldReturnEmptyList() {
        Transaction transaction = createTestTransaction(1L, TransactionStatus.PENDING);
        repository.save(transaction);
        TransactionFilter filter = mock(TransactionFilter.class);
        when(filter.test(any(Transaction.class))).thenReturn(false);

        List<Transaction> filteredTransactions = repository.findByFilter(filter);

        assertEquals(0, filteredTransactions.size());
    }
}