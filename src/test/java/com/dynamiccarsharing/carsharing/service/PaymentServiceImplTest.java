package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.PaymentDto;
import com.dynamiccarsharing.carsharing.dto.PaymentRequestDto;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.exception.PaymentNotFoundException;
import com.dynamiccarsharing.carsharing.mapper.PaymentMapper;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(paymentRepository, paymentMapper);
    }

    private Payment createTestPayment(Long id, TransactionStatus status) {
        return Payment.builder()
                .id(id)
                .amount(new BigDecimal("150.75"))
                .status(status)
                .build();
    }

    @Test
    void createPayment_shouldMapAndSaveAndReturnDto() {
        Long bookingId = 1L;
        PaymentRequestDto requestDto = new PaymentRequestDto();
        Payment paymentEntity = createTestPayment(null, TransactionStatus.PENDING);
        Payment savedEntity = createTestPayment(1L, TransactionStatus.PENDING);
        PaymentDto expectedDto = new PaymentDto();
        expectedDto.setId(1L);

        when(paymentMapper.toEntity(requestDto, bookingId)).thenReturn(paymentEntity);
        when(paymentRepository.save(paymentEntity)).thenReturn(savedEntity);
        when(paymentMapper.toDto(savedEntity)).thenReturn(expectedDto);

        PaymentDto result = paymentService.createPayment(bookingId, requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
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
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(Payment.builder().build()));
        doNothing().when(paymentRepository).deleteById(paymentId);

        paymentService.deleteById(paymentId);

        verify(paymentRepository).deleteById(paymentId);
    }


    @Test
    void confirmPayment_withPendingStatus_shouldSucceedAndReturnDto() {
        Long paymentId = 1L;
        Payment pendingPayment = createTestPayment(paymentId, TransactionStatus.PENDING);
        Payment completedEntity = pendingPayment.withStatus(TransactionStatus.COMPLETED);
        PaymentDto expectedDto = new PaymentDto();
        expectedDto.setStatus(TransactionStatus.COMPLETED);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(pendingPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(completedEntity);
        when(paymentMapper.toDto(completedEntity)).thenReturn(expectedDto);

        PaymentDto result = paymentService.confirmPayment(paymentId);

        assertNotNull(result);
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
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
        Payment refundedEntity = completedPayment.withStatus(TransactionStatus.REFUNDED);
        PaymentDto expectedDto = new PaymentDto();
        expectedDto.setStatus(TransactionStatus.REFUNDED);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(completedPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(refundedEntity);
        when(paymentMapper.toDto(refundedEntity)).thenReturn(expectedDto);

        PaymentDto result = paymentService.refundPayment(paymentId);

        assertNotNull(result);
        assertEquals(TransactionStatus.REFUNDED, result.getStatus());
    }

    @Test
    void refundPayment_withPendingStatus_shouldThrowException() {
        Long paymentId = 1L;
        Payment pendingPayment = createTestPayment(paymentId, TransactionStatus.PENDING);
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(pendingPayment));

        assertThrows(IllegalStateException.class, () -> paymentService.refundPayment(paymentId));
    }

    @Test
    void deleteById_whenPaymentDoesNotExist_shouldThrowException() {
        Long paymentId = 1L;
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.deleteById(paymentId));
        verify(paymentRepository, never()).deleteById(any());
    }
}