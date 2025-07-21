package com.dynamiccarsharing.carsharing.service.jdbc;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.exception.PaymentNotFoundException;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.PaymentFilter;
import com.dynamiccarsharing.carsharing.repository.jdbc.PaymentRepositoryJdbcImpl;
import com.dynamiccarsharing.carsharing.service.interfaces.PaymentService;
import com.dynamiccarsharing.carsharing.dto.PaymentSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("paymentService")
@Profile("jdbc")
@Transactional
public class PaymentServiceJdbcImpl implements PaymentService {

    private final PaymentRepositoryJdbcImpl paymentRepositoryJdbcImpl;

    public PaymentServiceJdbcImpl(PaymentRepositoryJdbcImpl paymentRepositoryJdbcImpl) {
        this.paymentRepositoryJdbcImpl = paymentRepositoryJdbcImpl;
    }

    @Override
    public Payment createPayment(Payment payment) {
        return paymentRepositoryJdbcImpl.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findById(Long id) {
        return paymentRepositoryJdbcImpl.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByBookingId(Long bookingId) {
        return paymentRepositoryJdbcImpl.findByBookingId(bookingId);
    }

    @Override
    public Payment confirmPayment(Long paymentId) {
        Payment payment = paymentRepositoryJdbcImpl.findById(paymentId).orElseThrow(() -> new PaymentNotFoundException("Payment with ID " + paymentId + " not found."));
        if (payment.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Payment must be PENDING to be confirmed.");
        }
        Payment confirmedPayment = payment.withStatus(TransactionStatus.COMPLETED);
        return paymentRepositoryJdbcImpl.save(confirmedPayment);
    }

    @Override
    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepositoryJdbcImpl.findById(paymentId).orElseThrow(() -> new PaymentNotFoundException("Payment with ID " + paymentId + " not found."));
        if (payment.getStatus() != TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Payment must be COMPLETED to be refunded.");
        }
        Payment refundedPayment = payment.withStatus(TransactionStatus.REFUNDED);
        return paymentRepositoryJdbcImpl.save(refundedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> findPaymentsByStatus(TransactionStatus status) {
        return paymentRepositoryJdbcImpl.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> searchPayments(PaymentSearchCriteria criteria) {
        Filter<Payment> filter = createFilterFromCriteria(criteria);
        try {
            return paymentRepositoryJdbcImpl.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search for payments failed", e);
        }
    }

    private Filter<Payment> createFilterFromCriteria(PaymentSearchCriteria criteria) {
        return PaymentFilter.of(
                null,
                criteria.getBookingId(),
                criteria.getAmount(),
                criteria.getStatus(),
                criteria.getPaymentMethod()
        );
    }
}