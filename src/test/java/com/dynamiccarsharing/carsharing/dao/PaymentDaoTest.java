package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.filter.PaymentFilter;
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
class PaymentDaoTest extends BaseDaoTest {
    @Autowired
    private PaymentDao paymentDao;

    private Booking booking1;
    private Booking booking2;

    @BeforeEach
    void setUp() throws SQLException {
        createTestDependencies();
    }

    private void createTestDependencies() throws SQLException {
        ContactInfo contactInfo = createContactInfo("payer@example.com", "+123", "Payer", "One");
        User testUser = createUser(contactInfo, UserRole.RENTER, UserStatus.ACTIVE);
        Location location = createLocation("Pay City", "PY", "54321");
        Car testCar = createCar("PAYCAR", "Subaru", "Outback", location);
        this.booking1 = createBooking(testUser, testCar, location, TransactionStatus.PENDING);
        this.booking2 = createBooking(testUser, testCar, location, TransactionStatus.COMPLETED);
    }

    private Payment createUnsavedPayment(Booking booking, BigDecimal amount, TransactionStatus status, PaymentType type) {
        return Payment.builder()
                .booking(booking)
                .amount(amount)
                .status(status)
                .paymentMethod(type)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save a new payment")
        void save_newPayment_shouldSaveSuccessfully() {
            Payment payment = createUnsavedPayment(booking1, BigDecimal.valueOf(150.00), TransactionStatus.PENDING, PaymentType.CREDIT_CARD);
            Payment saved = paymentDao.save(payment);
            assertNotNull(saved.getId());
            assertEquals(0, BigDecimal.valueOf(150.00).compareTo(saved.getAmount()));
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        @Test
        @DisplayName("Should find payment by valid ID")
        void findById_validId_shouldReturnPayment() {
            Payment saved = paymentDao.save(createUnsavedPayment(booking1, BigDecimal.valueOf(100.0), TransactionStatus.PENDING, PaymentType.CREDIT_CARD));
            Optional<Payment> found = paymentDao.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
        }

        @Test
        @DisplayName("Should find payment by booking ID")
        void findByBookingId_validId_shouldReturnPayment() {
            paymentDao.save(createUnsavedPayment(booking1, BigDecimal.valueOf(100.0), TransactionStatus.PENDING, PaymentType.CREDIT_CARD));
            Optional<Payment> found = paymentDao.findByBookingId(booking1.getId());
            assertTrue(found.isPresent());
            assertEquals(booking1.getId(), found.get().getBooking().getId());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {
        @Test
        @DisplayName("Should delete a payment")
        void deleteById_validId_shouldDelete() {
            Payment saved = paymentDao.save(createUnsavedPayment(booking1, BigDecimal.valueOf(100.0), TransactionStatus.PENDING, PaymentType.CREDIT_CARD));
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
            paymentDao.save(createUnsavedPayment(booking1, BigDecimal.valueOf(150.00), TransactionStatus.PENDING, PaymentType.CREDIT_CARD));
            paymentDao.save(createUnsavedPayment(booking2, BigDecimal.valueOf(200.00), TransactionStatus.COMPLETED, PaymentType.PAYPAL));
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
            assertEquals(booking2.getId(), results.get(0).getBooking().getId());
        }
    }
}