package com.dynamiccarsharing.booking.dao;

import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.filter.PaymentFilter;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.Payment;
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
class PaymentDaoTest extends BookingBaseDaoTest {
    @Autowired
    private PaymentDao paymentDao;

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

    private Payment createUnsavedPayment(Long bookingId, BigDecimal amount, TransactionStatus status, PaymentType type) {
        return Payment.builder()
                .booking(Booking.builder().id(bookingId).build())
                .amount(amount)
                .status(status)
                .paymentMethod(type)
                .build();
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save a new payment")
        void save_newPayment_shouldSaveSuccessfully() {
            Payment payment = createUnsavedPayment(bookingId1, BigDecimal.valueOf(150.00), TransactionStatus.PENDING, PaymentType.CREDIT_CARD);
            Payment saved = paymentDao.save(payment);
            assertNotNull(saved.getId());
            assertNotNull(saved.getCreatedAt());
            assertNull(saved.getUpdatedAt());
            assertEquals(0, BigDecimal.valueOf(150.00).compareTo(saved.getAmount()));
        }

        @Test
        @DisplayName("Should update an existing payment")
        void save_existingPayment_shouldUpdate() {
            Payment original = paymentDao.save(createUnsavedPayment(bookingId1, BigDecimal.valueOf(100.0), TransactionStatus.PENDING, PaymentType.CREDIT_CARD));

            original.setStatus(TransactionStatus.COMPLETED);
            original.setAmount(BigDecimal.valueOf(120.00));
            Payment updated = paymentDao.save(original);

            assertNotNull(updated.getUpdatedAt());
            assertEquals(original.getId(), updated.getId());
            assertEquals(TransactionStatus.COMPLETED, updated.getStatus());
            assertEquals(0, BigDecimal.valueOf(120.00).compareTo(updated.getAmount()));
        }

        @Test
        @DisplayName("Should throw DataAccessException on constraint violation")
        void save_invalidBookingId_shouldThrowException() {
            Payment payment = createUnsavedPayment(999L, BigDecimal.valueOf(100), TransactionStatus.PENDING, PaymentType.CREDIT_CARD);
            assertThrows(DataAccessException.class, () -> paymentDao.save(payment));
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        @Test
        @DisplayName("Should find payment by valid ID")
        void findById_validId_shouldReturnPayment() {
            Payment saved = paymentDao.save(createUnsavedPayment(bookingId1, BigDecimal.valueOf(100.0), TransactionStatus.PENDING, PaymentType.CREDIT_CARD));
            Optional<Payment> found = paymentDao.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
        }

        @Test
        @DisplayName("Should return empty Optional for invalid ID")
        void findById_invalidId_shouldReturnEmpty() {
            Optional<Payment> found = paymentDao.findById(999L);
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should find all payments")
        void findAll_shouldReturnAllPayments() {
            paymentDao.save(createUnsavedPayment(bookingId1, BigDecimal.valueOf(100.0), TransactionStatus.PENDING, PaymentType.CREDIT_CARD));
            paymentDao.save(createUnsavedPayment(bookingId2, BigDecimal.valueOf(200.0), TransactionStatus.COMPLETED, PaymentType.PAYPAL));
            List<Payment> all = paymentDao.findAll();
            assertEquals(2, all.size());
        }

        @Test
        @DisplayName("Should find payment by booking ID")
        void findByBookingId_validId_shouldReturnPayment() {
            paymentDao.save(createUnsavedPayment(bookingId1, BigDecimal.valueOf(100.0), TransactionStatus.PENDING, PaymentType.CREDIT_CARD));
            Optional<Payment> found = paymentDao.findByBookingId(bookingId1);
            assertTrue(found.isPresent());
            assertEquals(bookingId1, found.get().getBooking().getId());
        }

        @Test
        @DisplayName("Should return empty Optional for invalid booking ID")
        void findByBookingId_invalidId_shouldReturnEmpty() {
            Optional<Payment> found = paymentDao.findByBookingId(999L);
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should find payments by status")
        void findByStatus_validStatus_shouldReturnList() {
            paymentDao.save(createUnsavedPayment(bookingId1, BigDecimal.valueOf(100.0), TransactionStatus.PENDING, PaymentType.CREDIT_CARD));
            paymentDao.save(createUnsavedPayment(bookingId2, BigDecimal.valueOf(200.0), TransactionStatus.PENDING, PaymentType.PAYPAL));

            List<Payment> found = paymentDao.findByStatus(TransactionStatus.PENDING);
            assertEquals(2, found.size());
        }

        @Test
        @DisplayName("Should return empty list for status with no payments")
        void findByStatus_noMatchingStatus_shouldReturnEmptyList() {
            paymentDao.save(createUnsavedPayment(bookingId1, BigDecimal.valueOf(100.0), TransactionStatus.PENDING, PaymentType.CREDIT_CARD));
            List<Payment> found = paymentDao.findByStatus(TransactionStatus.COMPLETED);
            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {
        @Test
        @DisplayName("Should delete a payment")
        void deleteById_validId_shouldDelete() {
            Payment saved = paymentDao.save(createUnsavedPayment(bookingId1, BigDecimal.valueOf(100.0), TransactionStatus.PENDING, PaymentType.CREDIT_CARD));
            paymentDao.deleteById(saved.getId());
            Optional<Payment> found = paymentDao.findById(saved.getId());
            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperations {
        @BeforeEach
        void setUpData() {
            paymentDao.save(createUnsavedPayment(bookingId1, BigDecimal.valueOf(150.00), TransactionStatus.PENDING, PaymentType.CREDIT_CARD));
            paymentDao.save(createUnsavedPayment(bookingId2, BigDecimal.valueOf(200.00), TransactionStatus.COMPLETED, PaymentType.PAYPAL));
        }

        @Test
        @DisplayName("Should find payments by status filter")
        void findByFilter_byStatus_shouldReturnMatching() throws SQLException {
            PaymentFilter filter = PaymentFilter.ofStatus(TransactionStatus.COMPLETED);
            List<Payment> results = paymentDao.findByFilter(filter);
            assertEquals(1, results.size());
            assertEquals(TransactionStatus.COMPLETED, results.get(0).getStatus());
        }

        @Test
        @DisplayName("Should find payments by payment method filter")
        void findByFilter_byPaymentMethod_shouldReturnMatching() throws SQLException {
            PaymentFilter filter = PaymentFilter.ofPaymentMethod(PaymentType.CREDIT_CARD);
            List<Payment> results = paymentDao.findByFilter(filter);
            assertEquals(1, results.size());
            assertEquals(PaymentType.CREDIT_CARD, results.get(0).getPaymentMethod());
        }

        @Test
        @DisplayName("Should find payments by amount filter")
        void findByFilter_byAmount_shouldReturnMatching() throws SQLException {
            PaymentFilter filter = PaymentFilter.ofAmount(BigDecimal.valueOf(200.00));
            List<Payment> results = paymentDao.findByFilter(filter);
            assertEquals(1, results.size());
            assertEquals(bookingId2, results.get(0).getBooking().getId());
        }
    }
}