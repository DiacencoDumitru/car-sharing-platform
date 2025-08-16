package com.dynamiccarsharing.booking.service.interfaces;

import com.dynamiccarsharing.booking.criteria.PaymentSearchCriteria;
import com.dynamiccarsharing.contracts.dto.PaymentDto;
import com.dynamiccarsharing.contracts.dto.PaymentRequestDto;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    PaymentDto createPayment(Long bookingId, PaymentRequestDto requestDto);

    Optional<PaymentDto> findPaymentById(Long id);

    List<PaymentDto> findAllPayments();

    void deleteById(Long id);

    PaymentDto confirmPayment(Long paymentId);

    PaymentDto refundPayment(Long paymentId);

    Optional<PaymentDto> findByBookingId(Long bookingId);

    List<PaymentDto> searchPayments(PaymentSearchCriteria criteria);

    List<PaymentDto> findPaymentsByStatus(TransactionStatus status);
}