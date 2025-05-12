package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.InMemoryPaymentRepository;
import com.dynamiccarsharing.carsharing.repository.filter.PaymentFilter;
import com.dynamiccarsharing.carsharing.util.Validator;

import java.util.List;
import java.util.Optional;

public class PaymentService {

    private final InMemoryPaymentRepository inMemoryPaymentRepository;

    public PaymentService(InMemoryPaymentRepository inMemoryPaymentRepository) {
        this.inMemoryPaymentRepository = inMemoryPaymentRepository;
    }

    public Payment save(Payment payment) {
        Validator.validateNonNull(payment, "Payment");
        return inMemoryPaymentRepository.save(payment);
    }

    public Optional<Payment> findById(Long id) {
        Validator.validateId(id, "ID");
        return inMemoryPaymentRepository.findById(id);
    }

    public void delete(Long id) {
        Validator.validateId(id, "ID");
        inMemoryPaymentRepository.deleteById(id);
    }

    public Iterable<Payment> findAll() {
        return inMemoryPaymentRepository.findAll();
    }

    private Payment getPaymentOrThrow(Long id) {
        return inMemoryPaymentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Payment with ID " + id + " not found"));
    }

    public Payment approvePayment(Long id) {
        Payment payment = getPaymentOrThrow(id);
        validatePaymentStatus(payment.getStatus(), TransactionStatus.PENDING, "Payment can only be approved from PENDING status");
        return inMemoryPaymentRepository.save(payment.withStatus(TransactionStatus.APPROVED));
    }

    public Payment completePayment(Long id) {
        Payment payment = getPaymentOrThrow(id);
        validatePaymentStatus(payment.getStatus(), TransactionStatus.APPROVED, "Payment can only be completed from APPROVED status");
        return inMemoryPaymentRepository.save(payment.withStatus(TransactionStatus.COMPLETED));
    }

    public Payment cancelPayment(Long id) {
        Payment payment = getPaymentOrThrow(id);
        validatePaymentStatus(payment.getStatus(), TransactionStatus.COMPLETED, "Cannot cancel a completed payment");
        return inMemoryPaymentRepository.save(payment.withStatus(TransactionStatus.CANCELED));
    }

    private void validatePaymentStatus(TransactionStatus currentStatus, TransactionStatus expectedStatus, String errorMessage) {
        if (currentStatus != expectedStatus) {
            throw new IllegalStateException(errorMessage);
        }
    }

    public List<Payment> findPaymentsByBookingId(Long bookingId) {
        Validator.validateId(bookingId, "Booking ID");
        PaymentFilter filter = new PaymentFilter().setBookingId(bookingId);
        return (List<Payment>) inMemoryPaymentRepository.findByFilter(filter);
    }
}