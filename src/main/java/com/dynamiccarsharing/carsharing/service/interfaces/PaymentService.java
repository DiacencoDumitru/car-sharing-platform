package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.dto.PaymentDto;
import com.dynamiccarsharing.carsharing.dto.PaymentRequestDto;
import com.dynamiccarsharing.carsharing.dto.criteria.PaymentSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Payment;
<<<<<<< HEAD
=======
import com.dynamiccarsharing.carsharing.dto.criteria.PaymentSearchCriteria;
>>>>>>> fix/controller-mvc-tests

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    PaymentDto createPayment(Long bookingId, PaymentRequestDto requestDto);

    Optional<PaymentDto> findPaymentById(Long id);

    List<PaymentDto> findAllPayments();

    void deleteById(Long id);

    PaymentDto confirmPayment(Long paymentId);

    PaymentDto refundPayment(Long paymentId);

    List<Payment> findAll();

    void deleteById(Long id);

    Optional<Payment> findByBookingId(Long bookingId);

    List<Payment> searchPayments(PaymentSearchCriteria criteria);

    List<Payment> findPaymentsByStatus(TransactionStatus status);
}