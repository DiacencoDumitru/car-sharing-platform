package com.dynamiccarsharing.booking.dao;

import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.filter.TransactionFilter;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.Transaction;
import com.dynamiccarsharing.util.exception.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("jdbc")
class TransactionDaoTest extends BookingBaseDaoTest {
    @Autowired
    private TransactionDao transactionDao;

    private Long bookingId1;
    private Long bookingId2;

    @BeforeEach
    void setUp() {
        Long testUserId = 1L;
        Long testCarId = 10L;
        Long testLocationId = 100L;

        this.bookingId1 = createBooking(testUserId, testCarId, testLocationId, TransactionStatus.PENDING).getId();
        this.bookingId2 = createBooking(testUserId, testCarId, testLocationId, TransactionStatus.COMPLETED).getId();
    }

    private Transaction createUnsavedTransaction(Long bookingId, TransactionStatus status, PaymentType paymentType) {
        return Transaction.builder()
                .booking(Booking.builder().id(bookingId).build())
                .amount(BigDecimal.valueOf(150.00))
                .status(status)
                .paymentMethod(paymentType)
                .build();
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save a new transaction successfully")
        void save_newTransaction_shouldSave() {
            Transaction transaction = createUnsavedTransaction(bookingId1, TransactionStatus.COMPLETED, PaymentType.APPLE_PAY);
            Transaction saved = transactionDao.save(transaction);
            assertNotNull(saved.getId());
            assertNotNull(saved.getCreatedAt());
            assertNull(saved.getUpdatedAt());
            assertEquals(transaction.getBooking().getId(), saved.getBooking().getId());
        }

        @Test
        @DisplayName("Should update an existing transaction")
        void save_existingTransaction_shouldUpdate() {
            Transaction original = transactionDao.save(createUnsavedTransaction(bookingId1, TransactionStatus.PENDING, PaymentType.GOOGLE_PAY));

            original.setStatus(TransactionStatus.CANCELED);

            Transaction updated = transactionDao.save(original);
            assertNotNull(updated.getUpdatedAt());
            assertEquals(original.getId(), updated.getId());
            assertEquals(TransactionStatus.CANCELED, updated.getStatus());
        }

        @Test
        @DisplayName("Should throw DataAccessException on constraint violation")
        void save_invalidBookingId_shouldThrowException() {
            Transaction transaction = createUnsavedTransaction(999L, TransactionStatus.PENDING, PaymentType.CREDIT_CARD);
            assertThrows(DataAccessException.class, () -> transactionDao.save(transaction));
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        @Test
        @DisplayName("Should find transaction by valid ID")
        void findById_validId_shouldReturnTransaction() {
            Transaction saved = transactionDao.save(createUnsavedTransaction(bookingId1, TransactionStatus.COMPLETED, PaymentType.CREDIT_CARD));
            Optional<Transaction> found = transactionDao.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
        }

        @Test
        @DisplayName("Should return empty Optional for invalid ID")
        void findById_invalidId_shouldReturnEmpty() {
            Optional<Transaction> found = transactionDao.findById(999L);
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should find all transactions")
        void findAll_shouldReturnAllTransactions() {
            transactionDao.save(createUnsavedTransaction(bookingId1, TransactionStatus.COMPLETED, PaymentType.CREDIT_CARD));
            transactionDao.save(createUnsavedTransaction(bookingId2, TransactionStatus.PENDING, PaymentType.PAYPAL));
            List<Transaction> all = transactionDao.findAll();
            assertEquals(2, all.size());
        }

        @Test
        @DisplayName("Should find transactions by status")
        void findByStatus_validStatus_shouldReturnList() {
            transactionDao.save(createUnsavedTransaction(bookingId1, TransactionStatus.PENDING, PaymentType.CREDIT_CARD));
            transactionDao.save(createUnsavedTransaction(bookingId2, TransactionStatus.PENDING, PaymentType.PAYPAL));

            List<Transaction> found = transactionDao.findByStatus(TransactionStatus.PENDING);
            assertEquals(2, found.size());
        }

        @Test
        @DisplayName("Should return empty list for status with no transactions")
        void findByStatus_noMatchingStatus_shouldReturnEmptyList() {
            transactionDao.save(createUnsavedTransaction(bookingId1, TransactionStatus.PENDING, PaymentType.CREDIT_CARD));
            List<Transaction> found = transactionDao.findByStatus(TransactionStatus.COMPLETED);
            assertTrue(found.isEmpty());
        }

        @Test
        @DisplayName("Should find transactions by booking ID")
        void findByBookingId_validId_shouldReturnList() {
            transactionDao.save(createUnsavedTransaction(bookingId1, TransactionStatus.COMPLETED, PaymentType.CREDIT_CARD));
            transactionDao.save(createUnsavedTransaction(bookingId1, TransactionStatus.PENDING, PaymentType.PAYPAL));
            createUnsavedTransaction(bookingId2, TransactionStatus.PENDING, PaymentType.PAYPAL);


            List<Transaction> found = transactionDao.findByBookingId(bookingId1);
            assertEquals(2, found.size());
        }

        @Test
        @DisplayName("Should return empty list for invalid booking ID")
        void findByBookingId_invalidId_shouldReturnEmptyList() {
            List<Transaction> found = transactionDao.findByBookingId(999L);
            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {
        @Test
        @DisplayName("Should delete transaction by ID")
        void deleteById_validId_shouldDelete() {
            Transaction saved = transactionDao.save(createUnsavedTransaction(bookingId1, TransactionStatus.COMPLETED, PaymentType.CREDIT_CARD));
            transactionDao.deleteById(saved.getId());
            Optional<Transaction> found = transactionDao.findById(saved.getId());
            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperations {
        @BeforeEach
        void setUpData() {
            transactionDao.save(createUnsavedTransaction(bookingId1, TransactionStatus.COMPLETED, PaymentType.CREDIT_CARD));
            transactionDao.save(createUnsavedTransaction(bookingId2, TransactionStatus.PENDING, PaymentType.PAYPAL));
            transactionDao.save(createUnsavedTransaction(bookingId1, TransactionStatus.PENDING, PaymentType.CREDIT_CARD));
        }

        @Test
        @DisplayName("Should find transactions by booking ID")
        void findByFilter_byBookingId_shouldReturnMatching() throws SQLException {
            TransactionFilter filter = TransactionFilter.ofBookingId(bookingId1);
            List<Transaction> results = transactionDao.findByFilter(filter);
            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("Should find transactions by payment method")
        void findByFilter_byPaymentMethod_shouldReturnMatching() throws SQLException {
            TransactionFilter filter = TransactionFilter.ofPaymentMethod(PaymentType.PAYPAL);
            List<Transaction> results = transactionDao.findByFilter(filter);
            assertEquals(1, results.size());
            assertEquals(bookingId2, results.get(0).getBooking().getId());
        }

        @Test
        @DisplayName("Should return all transactions for empty filter")
        void findByFilter_emptyFilter_shouldReturnAll() throws SQLException {
            TransactionFilter filter = TransactionFilter.of(null, null, null);
            List<Transaction> results = transactionDao.findByFilter(filter);
            assertEquals(3, results.size());
        }
    }
}