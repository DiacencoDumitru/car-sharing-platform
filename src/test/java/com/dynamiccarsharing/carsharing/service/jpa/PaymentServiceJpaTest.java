package com.dynamiccarsharing.carsharing.service.jpa;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.jpa.PaymentJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceJpaTest {

    @Mock
    private PaymentJpaRepository paymentRepository;

    private PaymentServiceJpaImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceJpaImpl(paymentRepository);
    }

    private Payment createTestPayment(Long id, TransactionStatus status) {
        return Payment.builder()
                .id(id)
                .booking(Booking.builder().id(1L).build())
                .amount(new BigDecimal("150.75"))
                .status(status)
                .build();
    }

    @Test
    void createPayment_shouldCallRepositoryAndReturnPayment() {
        Payment paymentToSave = createTestPayment(null, TransactionStatus.PENDING);
        Payment savedPayment = createTestPayment(1L, TransactionStatus.PENDING);
        when(paymentRepository.save(paymentToSave)).thenReturn(savedPayment);

        Payment result = paymentService.createPayment(paymentToSave);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(0, new BigDecimal("150.75").compareTo(result.getAmount()));
        verify(paymentRepository).save(paymentToSave);
    }

    @Test
    void findById_whenPaymentExists_shouldReturnOptionalOfPayment() {
        Long paymentId = 1L;
        Payment testPayment = createTestPayment(paymentId, TransactionStatus.COMPLETED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));

        Optional<Payment> result = paymentService.findById(paymentId);

        assertTrue(result.isPresent());
        assertEquals(paymentId, result.get().getId());
    }

    @Test
    void confirmPayment_withPendingStatus_shouldSucceed() {
        Long paymentId = 1L;
        Payment pendingPayment = createTestPayment(paymentId, TransactionStatus.PENDING);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment confirmedPayment = paymentService.confirmPayment(paymentId);

        assertEquals(TransactionStatus.COMPLETED, confirmedPayment.getStatus());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void confirmPayment_withCompletedStatus_shouldThrowInvalidPaymentStatusException() {
        Long paymentId = 1L;
        Payment completedPayment = createTestPayment(paymentId, TransactionStatus.COMPLETED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(completedPayment));

        assertThrows(IllegalStateException.class, () -> paymentService.confirmPayment(paymentId));
    }

    @Test
    void refundPayment_withCompletedStatus_shouldSucceed() {
        Long paymentId = 1L;
        Payment completedPayment = createTestPayment(paymentId, TransactionStatus.COMPLETED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(completedPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment refundedPayment = paymentService.refundPayment(paymentId);

        assertEquals(TransactionStatus.REFUNDED, refundedPayment.getStatus());
    }

    @Test
    void findPaymentByBookingId_shouldCallRepository() {
        Long bookingId = 1L;
        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.of(createTestPayment(1L, TransactionStatus.PENDING)));

        paymentService.findByBookingId(bookingId);

        verify(paymentRepository).findByBookingId(bookingId);
    }

    @Test
    void findPaymentsByStatus_shouldCallRepository() {
        TransactionStatus status = TransactionStatus.APPROVED;
        when(paymentRepository.findByStatus(status)).thenReturn(List.of(createTestPayment(1L, status)));

        paymentService.findPaymentsByStatus(status);

        verify(paymentRepository).findByStatus(status);
    }
}