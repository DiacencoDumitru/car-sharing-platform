package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.exception.InvalidPaymentStatusException;
import com.dynamiccarsharing.carsharing.exception.PaymentNotFoundException;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository);
    }

    private Payment createTestPayment(UUID id, TransactionStatus status) {
        return Payment.builder()
                .id(id)
                .booking(Booking.builder().id(UUID.randomUUID()).build())
                .amount(new BigDecimal("150.75"))
                .status(status)
                .build();
    }

    @Test
    void save_shouldCallRepositoryAndReturnPayment() {
        Payment paymentToSave = createTestPayment(null, TransactionStatus.PENDING);
        Payment savedPayment = createTestPayment(UUID.randomUUID(), TransactionStatus.PENDING);
        when(paymentRepository.save(paymentToSave)).thenReturn(savedPayment);

        Payment result = paymentService.save(paymentToSave);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(0, new BigDecimal("150.75").compareTo(result.getAmount()));
        verify(paymentRepository).save(paymentToSave);
    }

    @Test
    void findById_whenPaymentExists_shouldReturnOptionalOfPayment() {
        UUID paymentId = UUID.randomUUID();
        Payment testPayment = createTestPayment(paymentId, TransactionStatus.COMPLETED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));

        Optional<Payment> result = paymentService.findById(paymentId);

        assertTrue(result.isPresent());
        assertEquals(paymentId, result.get().getId());
    }

    @Test
    void findById_whenPaymentDoesNotExist_shouldReturnEmptyOptional() {
        UUID paymentId = UUID.randomUUID();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        Optional<Payment> result = paymentService.findById(paymentId);

        assertFalse(result.isPresent());
    }

    @Test
    void deleteById_whenPaymentExists_shouldSucceed() {
        UUID paymentId = UUID.randomUUID();
        when(paymentRepository.existsById(paymentId)).thenReturn(true);
        doNothing().when(paymentRepository).deleteById(paymentId);

        paymentService.deleteById(paymentId);

        verify(paymentRepository).deleteById(paymentId);
    }

    @Test
    void deleteById_whenPaymentDoesNotExist_shouldThrowPaymentNotFoundException() {
        UUID paymentId = UUID.randomUUID();
        when(paymentRepository.existsById(paymentId)).thenReturn(false);

        assertThrows(PaymentNotFoundException.class, () -> {
            paymentService.deleteById(paymentId);
        });
    }

    @Test
    void findAll_shouldReturnListOfPayments() {
        when(paymentRepository.findAll()).thenReturn(List.of(createTestPayment(UUID.randomUUID(), TransactionStatus.PENDING)));

        List<Payment> results = paymentService.findAll();

        assertEquals(1, results.size());
    }

    @Test
    void approvePayment_withPendingStatus_shouldSucceed() {
        UUID paymentId = UUID.randomUUID();
        Payment pendingPayment = createTestPayment(paymentId, TransactionStatus.PENDING);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment approvedPayment = paymentService.approvePayment(paymentId);

        assertEquals(TransactionStatus.APPROVED, approvedPayment.getStatus());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void approvePayment_withCompletedStatus_shouldThrowInvalidPaymentStatusException() {
        UUID paymentId = UUID.randomUUID();
        Payment completedPayment = createTestPayment(paymentId, TransactionStatus.COMPLETED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(completedPayment));

        assertThrows(InvalidPaymentStatusException.class, () -> {
            paymentService.approvePayment(paymentId);
        });
    }

    @Test
    void completePayment_withApprovedStatus_shouldSucceed() {
        UUID paymentId = UUID.randomUUID();
        Payment approvedPayment = createTestPayment(paymentId, TransactionStatus.APPROVED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(approvedPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment completedPayment = paymentService.completePayment(paymentId);

        assertEquals(TransactionStatus.COMPLETED, completedPayment.getStatus());
    }

    @Test
    void cancelPayment_withPendingStatus_shouldSucceed() {
        UUID paymentId = UUID.randomUUID();
        Payment pendingPayment = createTestPayment(paymentId, TransactionStatus.PENDING);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment cancelledPayment = paymentService.cancelPayment(paymentId);

        assertEquals(TransactionStatus.CANCELED, cancelledPayment.getStatus());
    }

    @Test
    void findPaymentByBookingId_shouldCallRepository() {
        UUID bookingId = UUID.randomUUID();
        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.of(createTestPayment(UUID.randomUUID(), TransactionStatus.PENDING)));

        paymentService.findPaymentByBookingId(bookingId);

        verify(paymentRepository).findByBookingId(bookingId);
    }

    @Test
    void findPaymentsByStatus_shouldCallRepository() {
        TransactionStatus status = TransactionStatus.APPROVED;
        when(paymentRepository.findByStatus(status)).thenReturn(List.of(createTestPayment(UUID.randomUUID(), status)));

        paymentService.findPaymentsByStatus(status);

        verify(paymentRepository).findByStatus(status);
    }
}