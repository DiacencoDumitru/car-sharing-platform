package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.PaymentDto;
import com.dynamiccarsharing.carsharing.dto.PaymentRequestDto;
import com.dynamiccarsharing.carsharing.dto.criteria.PaymentSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.exception.PaymentNotFoundException;
import com.dynamiccarsharing.carsharing.exception.ServiceException;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.PaymentFilter;
import com.dynamiccarsharing.carsharing.mapper.PaymentMapper;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.PaymentRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("paymentService")
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentDto createPayment(Long bookingId, PaymentRequestDto requestDto) {
        Payment payment = paymentMapper.toEntity(requestDto, bookingId);
        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentDto> findPaymentById(Long id) {
        return paymentRepository.findById(id).map(paymentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDto> findAllPayments() {
        return paymentRepository.findAll().stream().map(paymentMapper::toDto).toList();
    }

    @Override
    public void deleteById(Long id) {
        if (paymentRepository.findById(id).isEmpty()) {
            throw new PaymentNotFoundException("Payment with ID " + id + " not found.");
        }
        paymentRepository.deleteById(id);
    }

    @Override
    public PaymentDto confirmPayment(Long paymentId) {
        Payment payment = getPaymentOrThrow(paymentId);
        if (payment.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Payment must be PENDING to be confirmed.");
        }
        Payment confirmedPayment = paymentRepository.save(payment.withStatus(TransactionStatus.COMPLETED));
        return paymentMapper.toDto(confirmedPayment);
    }

    @Override
    public PaymentDto refundPayment(Long paymentId) {
        Payment payment = getPaymentOrThrow(paymentId);
        if (payment.getStatus() != TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Payment must be COMPLETED to be refunded.");
        }
        Payment refundedPayment = paymentRepository.save(payment.withStatus(TransactionStatus.REFUNDED));
        return paymentMapper.toDto(refundedPayment);
    }

    private Payment getPaymentOrThrow(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment with ID " + paymentId + " not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
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
                criteria.getBookingId(),
                criteria.getAmount(),
                criteria.getStatus(),
                criteria.getPaymentMethod()
        );
        try {
            return paymentRepository.findByFilter(filter);
        } catch (SQLException e) {
            throw new ServiceException("Search for payments failed", e);
        }
    }
}