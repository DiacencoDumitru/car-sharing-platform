package com.dynamiccarsharing.booking.service.interfaces;

import com.dynamiccarsharing.booking.criteria.PaymentSearchCriteria;
import com.dynamiccarsharing.booking.dto.PaymentDto;
import com.dynamiccarsharing.booking.dto.PaymentRequestDto;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    PaymentDto createPayment(Long bookingId, PaymentRequestDto requestDto);

    Optional<PaymentDto> findPaymentById(Long id);

    List<PaymentDto> findAllPayments();

    void deleteById(Long id);

    PaymentDto confirmPayment(Long paymentId, Long actorUserId);

    PaymentDto refundPayment(Long paymentId, Long actorUserId);

    Optional<PaymentDto> findByBookingId(Long bookingId);

    List<PaymentDto> searchPayments(PaymentSearchCriteria criteria);

    List<PaymentDto> findPaymentsByStatus(TransactionStatus status);
}