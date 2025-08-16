package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.contracts.dto.PaymentDto;
import com.dynamiccarsharing.contracts.dto.PaymentRequestDto;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.exception.BookingNotFoundException;
import com.dynamiccarsharing.booking.exception.PaymentNotFoundException;
import com.dynamiccarsharing.booking.mapper.PaymentMapper;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.Payment;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private BookingRepository bookingRepository;

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(paymentRepository, paymentMapper, bookingRepository);
    }

    private Payment createTestPayment(Long id, TransactionStatus status) {
        return Payment.builder()
                .id(id)
                .amount(new BigDecimal("150.75"))
                .status(status)
                .build();
    }

    @Test
    @DisplayName("createPayment() should succeed when booking exists")
    void createPayment_shouldMapAndSaveAndReturnDto() {
        // Arrange
        Long bookingId = 10L;
        PaymentRequestDto requestDto = new PaymentRequestDto();

        Payment paymentEntity = createTestPayment(null, TransactionStatus.PENDING);
        Payment savedEntity = createTestPayment(1L, TransactionStatus.PENDING);
        PaymentDto expectedDto = new PaymentDto();
        expectedDto.setId(1L);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(new Booking()));

        when(paymentMapper.toEntity(requestDto)).thenReturn(paymentEntity);
        when(paymentRepository.save(paymentEntity)).thenReturn(savedEntity);
        when(paymentMapper.toDto(savedEntity)).thenReturn(expectedDto);

        PaymentDto result = paymentService.createPayment(bookingId, requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(bookingRepository).findById(bookingId);
    }

    @Test
    @DisplayName("createPayment() should throw exception when booking does not exist")
    void createPayment_whenBookingNotFound_shouldThrowException() {
        Long bookingId = 10L;
        PaymentRequestDto requestDto = new PaymentRequestDto();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> paymentService.createPayment(bookingId, requestDto));
    }

    @Test
    void findPaymentById_whenExists_shouldMapAndReturnDto() {
        Long paymentId = 1L;
        Payment paymentEntity = createTestPayment(paymentId, TransactionStatus.COMPLETED);
        PaymentDto expectedDto = new PaymentDto();
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentMapper.toDto(paymentEntity)).thenReturn(expectedDto);

        Optional<PaymentDto> result = paymentService.findPaymentById(paymentId);

        assertTrue(result.isPresent());
    }

    @Test
    void findAllPayments_shouldMapAndReturnDtoList() {
        Payment paymentEntity = createTestPayment(1L, TransactionStatus.COMPLETED);
        when(paymentRepository.findAll()).thenReturn(Collections.singletonList(paymentEntity));
        when(paymentMapper.toDto(paymentEntity)).thenReturn(new PaymentDto());

        List<PaymentDto> result = paymentService.findAllPayments();

        assertEquals(1, result.size());
    }


    @Test
    void deleteById_whenPaymentExists_shouldSucceed() {
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(createTestPayment(paymentId, TransactionStatus.PENDING)));
        doNothing().when(paymentRepository).deleteById(paymentId);

        paymentService.deleteById(paymentId);

        verify(paymentRepository).deleteById(paymentId);
    }

    @Test
    void deleteById_whenPaymentDoesNotExist_shouldThrowException() {
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.deleteById(paymentId));
        verify(paymentRepository, never()).deleteById(any());
    }

    @Test
    void confirmPayment_withPendingStatus_shouldSucceedAndReturnDto() {
        Long paymentId = 1L;
        Payment pendingPayment = createTestPayment(paymentId, TransactionStatus.PENDING);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(pendingPayment));

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        when(paymentRepository.save(paymentCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentMapper.toDto(any(Payment.class))).thenReturn(new PaymentDto());

        paymentService.confirmPayment(paymentId);

        assertEquals(TransactionStatus.COMPLETED, paymentCaptor.getValue().getStatus());
    }

    @Test
    void confirmPayment_withCompletedStatus_shouldThrowException() {
        Long paymentId = 1L;
        Payment completedPayment = createTestPayment(paymentId, TransactionStatus.COMPLETED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(completedPayment));

        assertThrows(IllegalStateException.class, () -> paymentService.confirmPayment(paymentId));
    }

    @Test
    void refundPayment_withCompletedStatus_shouldSucceedAndReturnDto() {
        Long paymentId = 1L;
        Payment completedPayment = createTestPayment(paymentId, TransactionStatus.COMPLETED);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(completedPayment));

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        when(paymentRepository.save(paymentCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentMapper.toDto(any(Payment.class))).thenReturn(new PaymentDto());

        paymentService.refundPayment(paymentId);

        assertEquals(TransactionStatus.REFUNDED, paymentCaptor.getValue().getStatus());
    }

    @Test
    void refundPayment_withPendingStatus_shouldThrowException() {
        Long paymentId = 1L;
        Payment pendingPayment = createTestPayment(paymentId, TransactionStatus.PENDING);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(pendingPayment));

        assertThrows(IllegalStateException.class, () -> paymentService.refundPayment(paymentId));
    }
}