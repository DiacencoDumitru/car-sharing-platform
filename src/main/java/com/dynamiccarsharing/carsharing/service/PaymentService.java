package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.exception.InvalidPaymentStatusException;
import com.dynamiccarsharing.carsharing.exception.PaymentNotFoundException;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment save(Payment payment) {
        return paymentRepository.save(payment);
    }

    public Optional<Payment> findById(UUID id) {
        return paymentRepository.findById(id);
    }

    public void deleteById(UUID id) {
        if (!paymentRepository.existsById(id)) {
            throw new PaymentNotFoundException("Payment with ID " + id + " not found.");
        }
        paymentRepository.deleteById(id);
    }

    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    public Payment approvePayment(UUID id) {
        Payment payment = getPaymentOrThrow(id);
        validatePaymentStatus(payment.getStatus(), List.of(TransactionStatus.PENDING), "Payment can only be approved from PENDING status");
        return paymentRepository.save(payment.withStatus(TransactionStatus.APPROVED));
    }

    public Payment completePayment(UUID id) {
        Payment payment = getPaymentOrThrow(id);
        validatePaymentStatus(payment.getStatus(), List.of(TransactionStatus.APPROVED), "Payment can only be completed from APPROVED status");
        return paymentRepository.save(payment.withStatus(TransactionStatus.COMPLETED));
    }

    public Payment cancelPayment(UUID id) {
        Payment payment = getPaymentOrThrow(id);
        validatePaymentStatus(payment.getStatus(), List.of(TransactionStatus.PENDING, TransactionStatus.APPROVED), "Cannot cancel a completed payment");
        return paymentRepository.save(payment.withStatus(TransactionStatus.CANCELED));
    }

    public Optional<Payment> findPaymentByBookingId(UUID bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    public List<Payment> findPaymentsByStatus(TransactionStatus status) {
        return paymentRepository.findByStatus(status);
    }

    private Payment getPaymentOrThrow(UUID id) {
        return paymentRepository.findById(id).orElseThrow(() -> new PaymentNotFoundException("Payment with ID " + id + " not found"));
    }

    private void validatePaymentStatus(TransactionStatus currentStatus, List<TransactionStatus> allowedStatuses, String errorMessage) {
        if (!allowedStatuses.contains(currentStatus)) {
            throw new InvalidPaymentStatusException(errorMessage);
        }
    }
}