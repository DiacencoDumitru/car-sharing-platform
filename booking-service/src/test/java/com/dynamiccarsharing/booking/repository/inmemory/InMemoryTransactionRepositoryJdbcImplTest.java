package com.dynamiccarsharing.booking.repository.inmemory;

import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.filter.TransactionFilter;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTransactionRepositoryJdbcImplTest {

    private InMemoryTransactionRepositoryJdbcImpl repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryTransactionRepositoryJdbcImpl();
    }

    private Transaction createTestTransaction(Long id, TransactionStatus status, Long bookingId, PaymentType paymentType) {
        LocalDateTime now = LocalDateTime.now();
        Booking booking = Booking.builder().id(bookingId).build();

        return Transaction.builder()
                .id(id)
                .booking(booking)
                .amount(BigDecimal.valueOf(100.0))
                .status(status)
                .paymentMethod(paymentType)
                .createdAt(now)
                .updatedAt(status == TransactionStatus.COMPLETED ? now : null)
                .build();
    }

    @Nested
    @DisplayName("CRUD and FindAll Operations")
    class CrudTests {
        @Test
        void save_shouldSaveAndReturnTransaction() {
            Transaction transaction = createTestTransaction(1L, TransactionStatus.PENDING, 10L, PaymentType.CREDIT_CARD);
            Transaction savedTransaction = repository.save(transaction);
            assertEquals(transaction, savedTransaction);
            assertTrue(repository.findById(1L).isPresent());
        }

        @Test
        void save_updateExistingTransaction_shouldChangeStatus() {
            Transaction original = createTestTransaction(1L, TransactionStatus.PENDING, 10L, PaymentType.CREDIT_CARD);
            repository.save(original);

            original.setStatus(TransactionStatus.COMPLETED);
            repository.save(original);

            Optional<Transaction> found = repository.findById(1L);
            assertTrue(found.isPresent());
            assertEquals(TransactionStatus.COMPLETED, found.get().getStatus());
        }

        @Test
        void findById_withExistingId_shouldReturnTransaction() {
            Transaction transaction = createTestTransaction(1L, TransactionStatus.PENDING, 10L, PaymentType.CREDIT_CARD);
            repository.save(transaction);
            Optional<Transaction> foundTransaction = repository.findById(1L);
            assertTrue(foundTransaction.isPresent());
            assertEquals(transaction, foundTransaction.get());
        }

        @Test
        void deleteById_withExistingId_shouldRemoveTransaction() {
            Transaction transaction = createTestTransaction(1L, TransactionStatus.PENDING, 10L, PaymentType.CREDIT_CARD);
            repository.save(transaction);
            repository.deleteById(1L);
            assertFalse(repository.findById(1L).isPresent());
        }

        @Test
        void findAll_withMultipleTransactions_shouldReturnAllTransactions() {
            Transaction tx1 = createTestTransaction(1L, TransactionStatus.PENDING, 10L, PaymentType.CREDIT_CARD);
            Transaction tx2 = createTestTransaction(2L, TransactionStatus.COMPLETED, 11L, PaymentType.PAYPAL);
            repository.save(tx1);
            repository.save(tx2);

            Iterable<Transaction> transactionsIterable = repository.findAll();
            List<Transaction> transactions = new ArrayList<>();
            transactionsIterable.forEach(transactions::add);

            assertEquals(2, transactions.size());
        }
    }

    @Nested
    @DisplayName("Custom Finder and Filter Operations")
    class FinderAndFilterTests {
        @Test
        @DisplayName("Should find transactions by status")
        void findByStatus_withMatchingTransactions_shouldReturnMatchingTransactions() {
            Transaction tx1 = createTestTransaction(1L, TransactionStatus.PENDING, 10L, PaymentType.CREDIT_CARD);
            Transaction tx2 = createTestTransaction(2L, TransactionStatus.COMPLETED, 11L, PaymentType.PAYPAL);
            repository.save(tx1);
            repository.save(tx2);

            List<Transaction> pending = repository.findByStatus(TransactionStatus.PENDING);
            assertEquals(1, pending.size());
            assertEquals(tx1, pending.get(0));
        }

        @Test
        @DisplayName("Should find transactions by booking ID")
        void findByBookingId_withMatchingTransactions_shouldReturnMatchingTransactions() {
            Transaction tx1 = createTestTransaction(1L, TransactionStatus.PENDING, 10L, PaymentType.CREDIT_CARD);
            Transaction tx2 = createTestTransaction(2L, TransactionStatus.COMPLETED, 11L, PaymentType.PAYPAL);
            Transaction tx3 = createTestTransaction(3L, TransactionStatus.COMPLETED, 10L, PaymentType.APPLE_PAY);
            repository.save(tx1);
            repository.save(tx2);
            repository.save(tx3);

            List<Transaction> booking1Txs = repository.findByBookingId(10L);
            assertEquals(2, booking1Txs.size());
            assertTrue(booking1Txs.contains(tx1));
            assertTrue(booking1Txs.contains(tx3));
        }

        @Test
        @DisplayName("Should find transactions by filter")
        void findByFilter_withMatchingTransactions_shouldReturnMatchingTransactions() {
            Transaction tx1 = createTestTransaction(1L, TransactionStatus.PENDING, 10L, PaymentType.CREDIT_CARD);
            Transaction tx2 = createTestTransaction(2L, TransactionStatus.COMPLETED, 11L, PaymentType.PAYPAL);
            repository.save(tx1);
            repository.save(tx2);

            TransactionFilter filter = TransactionFilter.ofPaymentMethod(PaymentType.PAYPAL);
            List<Transaction> filteredTransactions = repository.findByFilter(filter);
            assertEquals(1, filteredTransactions.size());
            assertEquals(tx2, filteredTransactions.get(0));
        }
    }
}