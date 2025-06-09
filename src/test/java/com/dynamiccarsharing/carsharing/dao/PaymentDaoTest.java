package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.filter.PaymentFilter;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PaymentDaoTest extends BaseDaoTest {
    private PaymentDao paymentDao;
    private Long bookingId1;
    private Long bookingId2;

    @BeforeEach
    void setUp() throws SQLException {
        paymentDao = new PaymentDao(databaseUtil);
        createTestDependencies();
    }

    private void createTestDependencies() throws SQLException {
        Long contactInfoId = createContactInfo("payer@example.com", "+123", "Payer", "One");
        Long userId = createUser(contactInfoId, "RENTER", "ACTIVE");
        Long locationId = createLocation("Pay City", "PY", "54321");
        Long carId = createCar("PAYCAR", "Subaru", "Outback", locationId);
        this.bookingId1 = createBooking(userId, carId, locationId, TransactionStatus.PENDING);
        this.bookingId2 = createBooking(userId, carId, locationId, TransactionStatus.COMPLETED);
    }


    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save a new payment")
        void save_newPayment_shouldSaveSuccessfully() {
            Payment payment = new Payment(null, bookingId1, 150.00, TransactionStatus.PENDING, PaymentType.CREDIT_CARD, LocalDateTime.now(), null);
            Payment saved = paymentDao.save(payment);
            assertNotNull(saved.getId());
            assertEquals(150.00, saved.getAmount());
        }
    }

    @Nested
    @DisplayName("Find and Filter Operations")
    class FindAndFilterOperations {
        @BeforeEach
        void setUpData() {
            paymentDao.save(new Payment(null, bookingId1, 150.00, TransactionStatus.PENDING, PaymentType.CREDIT_CARD, LocalDateTime.now(), null));
            paymentDao.save(new Payment(null, bookingId2, 200.00, TransactionStatus.COMPLETED, PaymentType.PAYPAL, LocalDateTime.now(), LocalDateTime.now()));
        }

        @Test
        @DisplayName("Should find payment by valid ID")
        void findById_validId_shouldReturnPayment() {
            List<Payment> all = (List<Payment>) paymentDao.findAll();
            Long idToFind = all.get(0).getId();
            Optional<Payment> found = paymentDao.findById(idToFind);
            assertTrue(found.isPresent());
            assertEquals(idToFind, found.get().getId());
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
        @DisplayName("Should return empty for non-matching filter")
        void findByFilter_noMatches_shouldReturnEmpty() throws SQLException {
            PaymentFilter filter = PaymentFilter.ofStatus(TransactionStatus.CANCELED);
            List<Payment> results = paymentDao.findByFilter(filter);
            assertTrue(results.isEmpty());
        }
    }
}