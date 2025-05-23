package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.PaymentRepository;
import com.dynamiccarsharing.carsharing.repository.filter.PaymentFilter;
import com.dynamiccarsharing.carsharing.util.Validator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment save(Payment payment) {
        Validator.validateNonNull(payment, "Payment");
        return paymentRepository.save(payment);
    }

    public Optional<Payment> findById(Long id) {
        Validator.validateId(id, "Payment ID");
        return paymentRepository.findById(id);
    }

    public void deleteById(Long id) {
        Validator.validateId(id, "Payment ID");
        paymentRepository.deleteById(id);
    }

    public Iterable<Payment> findAll() {
        return paymentRepository.findAll();
    }

    private Payment getPaymentOrThrow(Long id) {
        return paymentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Payment with ID " + id + " not found"));
    }

    public Payment approvePayment(Long id) {
        Payment payment = getPaymentOrThrow(id);
        validatePaymentStatus(payment.getStatus(), List.of(TransactionStatus.PENDING), "Payment can only be approved from PENDING status");
        return paymentRepository.save(payment.withStatus(TransactionStatus.APPROVED));
    }

    public Payment completePayment(Long id) {
        Payment payment = getPaymentOrThrow(id);
        validatePaymentStatus(payment.getStatus(), List.of(TransactionStatus.APPROVED), "Payment can only be completed from APPROVED status");
        return paymentRepository.save(payment.withUpdatedAt(LocalDateTime.now()).withStatus(TransactionStatus.COMPLETED));
    }

    public Payment cancelPayment(Long id) {
        Payment payment = getPaymentOrThrow(id);
        validatePaymentStatus(payment.getStatus(), List.of(TransactionStatus.PENDING, TransactionStatus.APPROVED), "Cannot cancel a completed payment");
        return paymentRepository.save(payment.withStatus(TransactionStatus.CANCELED));
    }

    private void validatePaymentStatus(TransactionStatus currentStatus, List<TransactionStatus> allowedStatuses, String errorMessage) {
        if (!allowedStatuses.contains(currentStatus)) {
            throw new IllegalStateException(errorMessage);
        }
    }

    public List<Payment> findPaymentsByBookingId(Long bookingId) {
        Validator.validateId(bookingId, "Booking ID");
        PaymentFilter filter = new PaymentFilter().setBookingId(bookingId);
        return paymentRepository.findByFilter(filter);
    }

    public List<Payment> findPaymentsByTransactionStatus(TransactionStatus transactionStatus) {
        Validator.validateNonNull(transactionStatus, "Transaction status");
        PaymentFilter filter = new PaymentFilter().setStatus(transactionStatus);
        return paymentRepository.findByFilter(filter);
    }
}