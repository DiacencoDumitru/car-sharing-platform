package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.model.*;
import com.dynamiccarsharing.carsharing.filter.TransactionFilter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("jdbc")
class TransactionDaoTest extends BaseDaoTest {
    @Autowired
    private TransactionDao transactionDao;

    private Booking booking1;
    private Booking booking2;

    @BeforeEach
    void setUp() throws SQLException {
        createTestDependencies();
    }

    private void createTestDependencies() throws SQLException {
        ContactInfo contactInfo = createContactInfo("renter@test.com", "111222", "Test", "Renter");
        User testUser = createUser(contactInfo, UserRole.RENTER, UserStatus.ACTIVE);
        Location location = createLocation("Test City", "TS", "12345");
        Car testCar = createCar("CAR1", "Tesla", "Model S", location);
        this.booking1 = createBooking(testUser, testCar, location, TransactionStatus.PENDING);
        this.booking2 = createBooking(testUser, testCar, location, TransactionStatus.COMPLETED);
    }

    private Transaction createUnsavedTransaction(Booking booking, TransactionStatus status, PaymentType paymentType) {
        return Transaction.builder()
                .booking(booking)
                .amount(BigDecimal.valueOf(150.00))
                .status(status)
                .paymentMethod(paymentType)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save a new transaction successfully")
        void save_newTransaction_shouldSave() {
            Transaction transaction = createUnsavedTransaction(booking1, TransactionStatus.COMPLETED, PaymentType.APPLE_PAY);
            Transaction saved = transactionDao.save(transaction);
            assertNotNull(saved.getId());
            assertEquals(transaction.getBooking().getId(), saved.getBooking().getId());
            assertEquals(transaction.getAmount(), saved.getAmount());
            assertEquals(transaction.getStatus(), saved.getStatus());
            assertNotNull(saved.getCreatedAt());
        }

        @Test
        @DisplayName("Should update an existing transaction")
        void save_existingTransaction_shouldUpdate() {
            Transaction original = transactionDao.save(createUnsavedTransaction(booking1, TransactionStatus.PENDING, PaymentType.GOOGLE_PAY));
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
            Transaction saved = transactionDao.save(createUnsavedTransaction(booking1, TransactionStatus.COMPLETED, PaymentType.CREDIT_CARD));
            Optional<Transaction> found = transactionDao.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {
        @Test
        @DisplayName("Should delete transaction by ID")
        void deleteById_validId_shouldDelete() {
            Transaction saved = transactionDao.save(createUnsavedTransaction(booking1, TransactionStatus.COMPLETED, PaymentType.CREDIT_CARD));
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
            transactionDao.save(createUnsavedTransaction(booking1, TransactionStatus.COMPLETED, PaymentType.CREDIT_CARD));
            transactionDao.save(createUnsavedTransaction(booking2, TransactionStatus.PENDING, PaymentType.PAYPAL));
            transactionDao.save(createUnsavedTransaction(booking1, TransactionStatus.PENDING, PaymentType.CREDIT_CARD));
        }

        @Test
        @DisplayName("Should find transactions by booking ID")
        void findByFilter_byBookingId_shouldReturnMatching() throws SQLException {
            TransactionFilter filter = TransactionFilter.ofBookingId(booking1.getId());
            List<Transaction> results = transactionDao.findByFilter(filter);
            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("Should find transactions by payment method")
        void findByFilter_byPaymentMethod_shouldReturnMatching() throws SQLException {
            TransactionFilter filter = TransactionFilter.ofPaymentMethod(PaymentType.PAYPAL);
            List<Transaction> results = transactionDao.findByFilter(filter);
            assertEquals(1, results.size());
            assertEquals(booking2.getId(), results.get(0).getBooking().getId());
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