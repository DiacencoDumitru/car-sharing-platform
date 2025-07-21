package com.dynamiccarsharing.carsharing.service.jpa;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.exception.PaymentNotFoundException;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.jpa.PaymentJpaRepository;
import com.dynamiccarsharing.carsharing.specification.PaymentSpecification;
import com.dynamiccarsharing.carsharing.service.interfaces.PaymentService;
import com.dynamiccarsharing.carsharing.dto.PaymentSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("paymentService")
@Profile("jpa")
@Transactional
public class PaymentServiceJpaImpl implements PaymentService {

    private final PaymentJpaRepository paymentRepository;

    public PaymentServiceJpaImpl(PaymentJpaRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findById(Long id) {
        return paymentRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    @Override
    public Payment confirmPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new PaymentNotFoundException("Payment with ID " + paymentId + " not found."));
        if (payment.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Payment must be PENDING to be confirmed.");
        }
        return paymentRepository.save(payment.withStatus(TransactionStatus.COMPLETED));
    }

    @Override
    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new PaymentNotFoundException("Payment with ID " + paymentId + " not found."));
        if (payment.getStatus() != TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Payment must be COMPLETED to be refunded.");
        }
        return paymentRepository.save(payment.withStatus(TransactionStatus.REFUNDED));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> searchPayments(PaymentSearchCriteria criteria) {
        return paymentRepository.findAll(
                PaymentSpecification.withCriteria(
                        criteria.getBookingId(),
                        criteria.getAmount(),
                        criteria.getStatus(),
                        criteria.getPaymentMethod()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findPaymentsByStatus(TransactionStatus status) {
        return paymentRepository.findByStatus(status);
    }
}