package com.dynamiccarsharing.carsharing.repository;


import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.filter.PaymentFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryPaymentRepositoryTest {

    private InMemoryPaymentRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPaymentRepository();
        repository.findAll().forEach(payment -> repository.deleteById(payment.getId()));
    }

    private Payment createTestPayment(Long id, TransactionStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return new Payment(id, 1L, 100.0, status, PaymentType.CREDIT_CARD, now, status == TransactionStatus.COMPLETED ? now : null);
    }

    @Test
    void save_shouldSaveAndReturnPayment() {
        Payment payment = createTestPayment(1L, TransactionStatus.PENDING);

        Payment savedPayment = repository.save(payment);

        assertEquals(payment, savedPayment);
        assertTrue(repository.findById(1L).isPresent());
        assertEquals(payment, repository.findById(1L).get());
    }

    @Test
    void save_withNullPayment_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
    }

    @Test
    void findById_withExistingId_shouldReturnPayment() {
        Payment payment = createTestPayment(1L, TransactionStatus.PENDING);
        repository.save(payment);

        Optional<Payment> foundPayment = repository.findById(1L);

        assertTrue(foundPayment.isPresent());
        assertEquals(payment, foundPayment.get());
    }

    @Test
    void findById_withNonExistingId_shouldReturnEmpty() {
        Optional<Payment> foundPayment = repository.findById(1L);

        assertFalse(foundPayment.isPresent());
    }

    @Test
    void deleteById_withExistingId_shouldRemovePayment() {
        Payment payment = createTestPayment(1L, TransactionStatus.PENDING);
        repository.save(payment);

        repository.deleteById(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void deleteById_withNonExistingId_shouldDoNothing() {
        repository.deleteById(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void findAll_withMultiplePayments_shouldReturnAllPayments() {
        Payment payment1 = createTestPayment(1L, TransactionStatus.PENDING);
        Payment payment2 = createTestPayment(2L, TransactionStatus.COMPLETED);
        repository.save(payment1);
        repository.save(payment2);

        Iterable<Payment> payments = repository.findAll();
        List<Payment> paymentList = new ArrayList<>();
        payments.forEach(paymentList::add);

        assertEquals(2, paymentList.size());
        assertTrue(paymentList.contains(payment1));
        assertTrue(paymentList.contains(payment2));
    }

    @Test
    void findAll_withSinglePayment_shouldReturnSinglePayment() {
        Payment payment = createTestPayment(1L, TransactionStatus.PENDING);
        repository.save(payment);

        Iterable<Payment> payments = repository.findAll();
        List<Payment> paymentList = new ArrayList<>();
        payments.forEach(paymentList::add);

        assertEquals(1, paymentList.size());
        assertEquals(payment, paymentList.get(0));
    }

    @Test
    void findAll_withNoPayments_shouldReturnEmptyIterable() {
        Iterable<Payment> payments = repository.findAll();
        List<Payment> paymentList = new ArrayList<>();
        payments.forEach(paymentList::add);

        assertEquals(0, paymentList.size());
    }

    @Test
    void findByFilter_withMatchingPayments_shouldReturnMatchingPayments() {
        Payment payment1 = createTestPayment(1L, TransactionStatus.PENDING);
        Payment payment2 = createTestPayment(2L, TransactionStatus.COMPLETED);
        Payment payment3 = createTestPayment(3L, TransactionStatus.PENDING);
        repository.save(payment1);
        repository.save(payment2);
        repository.save(payment3);
        PaymentFilter filter = mock(PaymentFilter.class);
        when(filter.test(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            return payment.getStatus() == TransactionStatus.PENDING;
        });

        List<Payment> filteredPayments = repository.findByFilter(filter);

        assertEquals(2, filteredPayments.size());
        assertTrue(filteredPayments.contains(payment1));
        assertTrue(filteredPayments.contains(payment3));
        assertFalse(filteredPayments.contains(payment2));
    }

    @Test
    void findByFilter_withNoMatchingPayments_shouldReturnEmptyList() {
        Payment payment = createTestPayment(1L, TransactionStatus.PENDING);
        repository.save(payment);
        PaymentFilter filter = mock(PaymentFilter.class);
        when(filter.test(any(Payment.class))).thenReturn(false);

        List<Payment> filteredPayments = repository.findByFilter(filter);

        assertEquals(0, filteredPayments.size());
    }
}