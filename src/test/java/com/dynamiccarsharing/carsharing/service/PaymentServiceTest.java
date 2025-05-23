package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.PaymentRepository;
import com.dynamiccarsharing.carsharing.repository.filter.PaymentFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    PaymentRepository paymentRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reset(paymentRepository);
        paymentService = new PaymentService(paymentRepository);
    }

    private Payment createTestPayment(TransactionStatus status) {
        return new Payment(1L, 1L, 100.0, status != null ? status : TransactionStatus.PENDING, PaymentType.CREDIT_CARD, LocalDateTime.now(), null);
    }

    @Test
    void save_shouldCallRepository_shouldReturnSamePayment() {
        Payment payment = createTestPayment(TransactionStatus.PENDING);
        when(paymentRepository.save(payment)).thenReturn(payment);

        Payment savedPayment = paymentService.save(payment);

        verify(paymentRepository, times(1)).save(payment);
        assertSame(payment, savedPayment);
        assertEquals(payment.getId(), savedPayment.getId());
        assertEquals(payment.getBookingId(), savedPayment.getBookingId());
        assertEquals(payment.getAmount(), savedPayment.getAmount(), 0.001);
        assertEquals(payment.getStatus(), savedPayment.getStatus());
        assertEquals(payment.getPaymentMethod(), savedPayment.getPaymentMethod());
        assertEquals(payment.getCreatedAt(), savedPayment.getCreatedAt());
        assertEquals(payment.getUpdatedAt(), savedPayment.getUpdatedAt());
    }

    @Test
    void save_whenPaymentIsNull_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> paymentService.save(null));

        assertEquals("Payment must be non-null", exception.getMessage());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void findById_whenPaymentIsPresent_shouldReturnPayment() {
        Payment payment = createTestPayment(TransactionStatus.PENDING);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        Optional<Payment> foundPayment = paymentService.findById(1L);

        verify(paymentRepository, times(1)).findById(1L);
        assertTrue(foundPayment.isPresent());
        assertSame(payment, foundPayment.get());
        assertEquals(payment.getId(), foundPayment.get().getId());
        assertEquals(payment.getBookingId(), foundPayment.get().getBookingId());
        assertEquals(payment.getAmount(), foundPayment.get().getAmount(), 0.001);
        assertEquals(TransactionStatus.PENDING, foundPayment.get().getStatus());
    }

    @Test
    void findById_whenPaymentNotFound_shouldReturnEmpty() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Payment> foundPayment = paymentService.findById(1L);

        verify(paymentRepository, times(1)).findById(1L);
        assertFalse(foundPayment.isPresent());
    }

    @Test
    void findById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> paymentService.findById(-1L));

        assertEquals("Payment ID must be non-negative", exception.getMessage());
        verify(paymentRepository, never()).findById(any());
    }

    @Test
    void deleteById_withValidId_shouldDeletePayment() {
        paymentService.deleteById(1L);

        verify(paymentRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> paymentService.deleteById(-1L));

        assertEquals("Payment ID must be non-negative", exception.getMessage());
        verify(paymentRepository, never()).findById(any());
    }

    @Test
    void findAll_withMultiplePayments_shouldReturnAllPayments() {
        Payment payment1 = createTestPayment(TransactionStatus.PENDING);
        Payment payment2 = new Payment(2L, 2L, 200.0, TransactionStatus.APPROVED, PaymentType.PAYPAL, LocalDateTime.now().minusHours(1), null);
        List<Payment> payments = Arrays.asList(payment1, payment2);
        when(paymentRepository.findAll()).thenReturn(payments);

        Iterable<Payment> result = paymentService.findAll();

        verify(paymentRepository, times(1)).findAll();
        List<Payment> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(payment1));
        assertTrue(resultList.contains(payment2));
        assertEquals(payment1.getId(), resultList.get(0).getId());
        assertEquals(payment1.getBookingId(), resultList.get(0).getBookingId());
        assertEquals(payment1.getAmount(), resultList.get(0).getAmount(), 0.001);
        assertEquals(payment1.getStatus(), resultList.get(0).getStatus());
    }

    @Test
    void findAll_withSinglePayment_shouldReturnSinglePayment() {
        Payment payment = createTestPayment(TransactionStatus.PENDING);
        List<Payment> payments = Collections.singletonList(payment);
        when(paymentRepository.findAll()).thenReturn(payments);

        Iterable<Payment> result = paymentService.findAll();

        verify(paymentRepository, times(1)).findAll();
        List<Payment> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(1, resultList.size());
        assertSame(payment, resultList.get(0));
        assertEquals(payment.getId(), resultList.get(0).getId());
        assertEquals(payment.getBookingId(), resultList.get(0).getBookingId());
        assertEquals(payment.getAmount(), resultList.get(0).getAmount(), 0.001);
        assertEquals(payment.getStatus(), resultList.get(0).getStatus());
    }

    @Test
    void findAll_withNoPayments_shouldReturnEmptyIterable() {
        List<Payment> payments = Collections.emptyList();
        when(paymentRepository.findAll()).thenReturn(payments);

        Iterable<Payment> result = paymentService.findAll();

        verify(paymentRepository, times(1)).findAll();
        List<Payment> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(0, resultList.size());
    }

    @Test
    void approvePayment_withPendingStatus_shouldSetApproved() {
        Payment pendingPayment = createTestPayment(TransactionStatus.PENDING);
        Payment approvedPayment = pendingPayment.withStatus(TransactionStatus.APPROVED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(approvedPayment);

        Payment result = paymentService.approvePayment(1L);

        assertEquals(TransactionStatus.APPROVED, result.getStatus());
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(approvedPayment);
    }

    @Test
    void approvePayment_withNonPendingStatus_shouldThrowIllegalStateException() {
        Payment approvedPayment = createTestPayment(TransactionStatus.APPROVED);
        when(paymentRepository.findById(2L)).thenReturn(Optional.of(approvedPayment));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> paymentService.approvePayment(2L));

        assertEquals("Payment can only be approved from PENDING status", exception.getMessage());
        verify(paymentRepository, times(1)).findById(2L);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void completePayment_withApprovedStatus_shouldSetCompleted() {
        Payment approvedPayment = createTestPayment(TransactionStatus.APPROVED);
        Payment completedPayment = approvedPayment.withUpdatedAt(LocalDateTime.now()).withStatus(TransactionStatus.COMPLETED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(approvedPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(completedPayment);

        Payment result = paymentService.completePayment(1L);

        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getUpdatedAt());
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(argThat(savedPayment -> savedPayment.getId().equals(1L) && savedPayment.getStatus() == TransactionStatus.COMPLETED && savedPayment.getUpdatedAt() != null));
    }

    @Test
    void completePayment_withNonApprovedStatus_shouldThrowIllegalStateException() {
        Payment pendingPayment = createTestPayment(TransactionStatus.PENDING);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pendingPayment));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> paymentService.completePayment(1L));

        assertEquals("Payment can only be completed from APPROVED status", exception.getMessage());
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void completePayment_withNonExistentId_shouldThrowIllegalArgumentException() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> paymentService.completePayment(1L));

        assertEquals("Payment with ID " + 1L + " not found", exception.getMessage());
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void cancelPayment_withPendingStatus_shouldSetCanceled() {
        Payment pendingPayment = createTestPayment(TransactionStatus.PENDING);
        Payment canceledPayment = pendingPayment.withStatus(TransactionStatus.CANCELED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(canceledPayment);

        Payment result = paymentService.cancelPayment(1L);

        assertEquals(TransactionStatus.CANCELED, result.getStatus());
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(canceledPayment);
    }

    @Test
    void cancelPayment_withApprovedStatus_shouldSetCanceled() {
        Payment approvedPayment = createTestPayment(TransactionStatus.APPROVED);
        Payment canceledPayment = approvedPayment.withStatus(TransactionStatus.CANCELED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(approvedPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(canceledPayment);

        Payment result = paymentService.cancelPayment(1L);

        assertEquals(TransactionStatus.CANCELED, result.getStatus());
        verify(paymentRepository, times(1)).findById(1L);
        verify(paymentRepository, times(1)).save(canceledPayment);
    }

    @Test
    void findPaymentsByBookingId_withValidId_shouldReturnBookings() {
        Payment payment = createTestPayment(TransactionStatus.APPROVED);
        List<Payment> payments = List.of(payment);
        when(paymentRepository.findByFilter(argThat(filter -> filter != null && filter.test(payment) && payment.getBookingId().equals(1L)))).thenReturn(payments);

        List<Payment> result = paymentService.findPaymentsByBookingId(1L);

        assertEquals(1, result.size());
        assertEquals(payment, result.get(0));
        verify(paymentRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(payment) && payment.getBookingId().equals(1L)));
    }

    @Test
    void findPaymentsByTransactionStatus_withValidStatus_shouldReturnPayments() {
        Payment payment = createTestPayment(TransactionStatus.PENDING);
        List<Payment> payments = List.of(payment);
        when(paymentRepository.findByFilter(argThat(filter -> filter != null && filter.test(payment) && payment.getStatus().equals(TransactionStatus.PENDING)))).thenReturn(payments);

        List<Payment> result = paymentService.findPaymentsByTransactionStatus(TransactionStatus.PENDING);

        assertEquals(payments, result);
        assertEquals(1, result.size());
        assertEquals(TransactionStatus.PENDING, result.get(0).getStatus());
        verify(paymentRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(payment) && payment.getStatus().equals(TransactionStatus.PENDING)));
    }
}