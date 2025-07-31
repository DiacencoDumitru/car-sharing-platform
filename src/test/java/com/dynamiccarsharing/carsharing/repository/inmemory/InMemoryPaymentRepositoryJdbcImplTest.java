package com.dynamiccarsharing.carsharing.repository.inmemory;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.filter.PaymentFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryPaymentRepositoryJdbcImplTest {

    private InMemoryPaymentRepositoryJdbcImpl repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPaymentRepositoryJdbcImpl();
    }

    private Payment createTestPayment(Long id, TransactionStatus status, Long bookingId) {
        LocalDateTime now = LocalDateTime.now();
        Booking booking = Booking.builder().id(bookingId).build();

        return Payment.builder()
                .id(id)
                .booking(booking)
                .amount(BigDecimal.valueOf(100.0))
                .status(status)
                .paymentMethod(PaymentType.CREDIT_CARD)
                .createdAt(now)
                .updatedAt(status == TransactionStatus.COMPLETED ? now : null)
                .build();
    }

    @Nested
    @DisplayName("CRUD and FindAll Operations")
    class CrudTests {
        @Test
        void save_shouldSaveAndReturnPayment() {
            Payment payment = createTestPayment(1L, TransactionStatus.PENDING, 10L);
            Payment savedPayment = repository.save(payment);
            assertEquals(payment, savedPayment);
            assertTrue(repository.findById(1L).isPresent());
        }

        @Test
        void save_updateExistingPayment_shouldChangeStatus() {
            Payment original = createTestPayment(1L, TransactionStatus.PENDING, 10L);
            repository.save(original);

            Payment updated = original.withStatus(TransactionStatus.COMPLETED);
            repository.save(updated);

            Optional<Payment> found = repository.findById(1L);
            assertTrue(found.isPresent());
            assertEquals(TransactionStatus.COMPLETED, found.get().getStatus());
        }

        @Test
        void findById_withExistingId_shouldReturnPayment() {
            Payment payment = createTestPayment(1L, TransactionStatus.PENDING, 10L);
            repository.save(payment);
            Optional<Payment> foundPayment = repository.findById(1L);
            assertTrue(foundPayment.isPresent());
            assertEquals(payment, foundPayment.get());
        }

        @Test
        void findById_withNonExistentId_shouldReturnEmpty() {
            Optional<Payment> found = repository.findById(999L);
            assertTrue(found.isEmpty());
        }

        @Test
        void deleteById_withExistingId_shouldRemovePayment() {
            Payment payment = createTestPayment(1L, TransactionStatus.PENDING, 10L);
            repository.save(payment);
            repository.deleteById(1L);
            assertFalse(repository.findById(1L).isPresent());
        }

        @Test
        void findAll_withMultiplePayments_shouldReturnAllPayments() {
            Payment payment1 = createTestPayment(1L, TransactionStatus.PENDING, 10L);
            Payment payment2 = createTestPayment(2L, TransactionStatus.COMPLETED, 11L);
            repository.save(payment1);
            repository.save(payment2);

            Iterable<Payment> paymentsIterable = repository.findAll();
            List<Payment> payments = new ArrayList<>();
            paymentsIterable.forEach(payments::add);

            assertEquals(2, payments.size());
        }
    }

    @Nested
    @DisplayName("Custom Finder and Filter Operations")
    class FinderAndFilterTests {
        @Test
        @DisplayName("Should find payment by booking ID")
        void findByBookingId_withMatchingPayment_shouldReturnPayment() {
            Payment payment1 = createTestPayment(1L, TransactionStatus.PENDING, 10L);
            Payment payment2 = createTestPayment(2L, TransactionStatus.COMPLETED, 11L);
            repository.save(payment1);
            repository.save(payment2);

            Optional<Payment> found = repository.findByBookingId(11L);
            assertTrue(found.isPresent());
            assertEquals(payment2, found.get());
        }

        @Test
        @DisplayName("Should find payments by status")
        void findByStatus_withMatchingPayments_shouldReturnMatchingPayments() {
            Payment payment1 = createTestPayment(1L, TransactionStatus.PENDING, 10L);
            Payment payment2 = createTestPayment(2L, TransactionStatus.COMPLETED, 11L);
            repository.save(payment1);
            repository.save(payment2);

            List<Payment> pendingPayments = repository.findByStatus(TransactionStatus.PENDING);
            assertEquals(1, pendingPayments.size());
            assertEquals(payment1, pendingPayments.get(0));
        }

        @Test
        @DisplayName("Should find payments by filter")
        void findByFilter_withMatchingPayments_shouldReturnMatchingPayments() {
            Payment payment1 = createTestPayment(1L, TransactionStatus.PENDING, 10L);
            Payment payment2 = createTestPayment(2L, TransactionStatus.COMPLETED, 11L);
            repository.save(payment1);
            repository.save(payment2);

            PaymentFilter filter = PaymentFilter.ofStatus(TransactionStatus.PENDING);
            List<Payment> filteredPayments = repository.findByFilter(filter);
            assertEquals(1, filteredPayments.size());
            assertEquals(payment1, filteredPayments.get(0));
        }
    }
}