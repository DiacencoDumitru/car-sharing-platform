package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.dto.criteria.PaymentSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface PaymentService {
    Payment createPayment(Payment payment);

    Optional<Payment> findById(Long id);

    List<Payment> findAll();

    void deleteById(Long id);

    Optional<Payment> findByBookingId(Long bookingId);

    Payment confirmPayment(Long paymentId);

    Payment refundPayment(Long paymentId);

    List<Payment> searchPayments(PaymentSearchCriteria criteria);

    List<Payment> findPaymentsByStatus(TransactionStatus status);
}