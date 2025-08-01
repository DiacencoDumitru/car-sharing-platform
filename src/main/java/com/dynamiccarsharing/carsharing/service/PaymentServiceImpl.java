package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.criteria.PaymentSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.exception.PaymentNotFoundException;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.PaymentFilter;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.PaymentRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("paymentService")
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
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
    public List<Payment> findAll() {
        return (List<Payment>) paymentRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        if (paymentRepository.findById(id).isEmpty()) {
            throw new PaymentNotFoundException("Payment with ID " + id + " not found.");
        }
        paymentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    @Override
    public Payment confirmPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment with ID " + paymentId + " not found."));
        if (payment.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Payment must be PENDING to be confirmed.");
        }
        return paymentRepository.save(payment.withStatus(TransactionStatus.COMPLETED));
    }

    @Override
    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment with ID " + paymentId + " not found."));
        if (payment.getStatus() != TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Payment must be COMPLETED to be refunded.");
        }
        return paymentRepository.save(payment.withStatus(TransactionStatus.REFUNDED));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findPaymentsByStatus(TransactionStatus status) {
        return paymentRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> searchPayments(PaymentSearchCriteria criteria) {
        Filter<Payment> filter = PaymentFilter.of(
                null,
                criteria.getBookingId(),
                criteria.getAmount(),
                criteria.getStatus(),
                criteria.getPaymentMethod()
        );
        try {
            return paymentRepository.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search for payments failed", e);
        }
    }
}