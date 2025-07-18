package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Transaction;
import com.dynamiccarsharing.carsharing.repository.filter.TransactionFilter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TransactionDaoTest extends BaseDaoTest {
    @Autowired
    private TransactionDao transactionDao;

    private Long bookingId1;
    private Long bookingId2;

    @BeforeEach
    void setUp() throws SQLException {
        createTestDependencies();
    }

    private void createTestDependencies() throws SQLException {
        Long contactInfoId = createContactInfo("renter@test.com", "111222", "Test", "Renter");
        Long userId = createUser(contactInfoId, "RENTER", "ACTIVE");
        Long locationId = createLocation("Test City", "TS", "12345");
        Long carId = createCar("CAR1", "Tesla", "Model S", locationId);
        this.bookingId1 = createBooking(userId, carId, locationId, TransactionStatus.PENDING);
        this.bookingId2 = createBooking(userId, carId, locationId, TransactionStatus.PENDING);
    }

    private Transaction createTestTransaction(Long bookingId, TransactionStatus status) {
        return new Transaction(null, bookingId, 150.00, status, PaymentType.APPLE_PAY, LocalDateTime.now(), null);
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save a new transaction successfully")
        void save_newTransaction_shouldSave() {
            Transaction transaction = createTestTransaction(bookingId1, TransactionStatus.COMPLETED);
            Transaction saved = transactionDao.save(transaction);

            assertNotNull(saved.getId());
            assertEquals(transaction.getBooking_id(), saved.getBooking_id());
            assertEquals(transaction.getAmount(), saved.getAmount());
            assertEquals(transaction.getStatus(), saved.getStatus());
            assertNotNull(saved.getCreatedAt());
        }

        @Test
        @DisplayName("Should update an existing transaction")
        void save_existingTransaction_shouldUpdate() {
            Transaction original = transactionDao.save(createTestTransaction(bookingId1, TransactionStatus.PENDING));
            Transaction toUpdate = original.withStatus(TransactionStatus.CANCELED).withUpdatedAt(LocalDateTime.now());
            Transaction updated = transactionDao.save(toUpdate);

            assertEquals(original.getId(), updated.getId());
            assertEquals(TransactionStatus.CANCELED, updated.getStatus());
            assertNotNull(updated.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        @Test
        @DisplayName("Should find transaction by valid ID")
        void findById_validId_shouldReturnTransaction() {
            Transaction saved = transactionDao.save(createTestTransaction(bookingId1, TransactionStatus.COMPLETED));
            Optional<Transaction> found = transactionDao.findById(saved.getId());

            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
        }

        @Test
        @DisplayName("Should return empty for non-existent ID")
        void findById_nonExistentId_shouldReturnEmpty() {
            Optional<Transaction> found = transactionDao.findById(999L);
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should find all transactions")
        void findAll_withData_shouldReturnAll() {
            transactionDao.save(createTestTransaction(bookingId1, TransactionStatus.COMPLETED));
            transactionDao.save(createTestTransaction(bookingId2, TransactionStatus.COMPLETED));
            List<Transaction> transactions = (List<Transaction>) transactionDao.findAll();
            assertEquals(2, transactions.size());
        }

        @Test
        @DisplayName("Should return empty list when no transactions exist")
        void findAll_noData_shouldReturnEmpty() {
            List<Transaction> transactions = (List<Transaction>) transactionDao.findAll();
            assertTrue(transactions.isEmpty());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {
        @Test
        @DisplayName("Should delete transaction by ID")
        void deleteById_validId_shouldDelete() {
            Transaction saved = transactionDao.save(createTestTransaction(bookingId1, TransactionStatus.COMPLETED));
            transactionDao.deleteById(saved.getId());
            Optional<Transaction> found = transactionDao.findById(saved.getId());
            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperations {
        @Test
        @DisplayName("Should find transactions by booking ID")
        void findByFilter_byBookingId_shouldReturnMatching() throws SQLException {
            transactionDao.save(createTestTransaction(bookingId1, TransactionStatus.COMPLETED));
            transactionDao.save(createTestTransaction(bookingId2, TransactionStatus.PENDING));

            TransactionFilter filter = TransactionFilter.ofBookingId(bookingId2);
            List<Transaction> results = transactionDao.findByFilter(filter);

            assertEquals(1, results.size());
            assertEquals(bookingId2, results.get(0).getBooking_id());
        }

        @Test
        @DisplayName("Should find transactions by status")
        void findByFilter_byStatus_shouldReturnMatching() throws SQLException {
            transactionDao.save(createTestTransaction(bookingId1, TransactionStatus.COMPLETED));
            transactionDao.save(createTestTransaction(bookingId2, TransactionStatus.PENDING));

            TransactionFilter filter = TransactionFilter.ofStatus(TransactionStatus.COMPLETED);
            List<Transaction> results = transactionDao.findByFilter(filter);

            assertEquals(1, results.size());
            assertEquals(TransactionStatus.COMPLETED, results.get(0).getStatus());
        }

        @Test
        @DisplayName("Should return all transactions for null filter")
        void findByFilter_nullFilter_shouldReturnAll() throws SQLException {
            transactionDao.save(createTestTransaction(bookingId1, TransactionStatus.COMPLETED));
            transactionDao.save(createTestTransaction(bookingId2, TransactionStatus.PENDING));

            List<Transaction> results = transactionDao.findByFilter(null);
            assertEquals(2, results.size());
        }
    }
}