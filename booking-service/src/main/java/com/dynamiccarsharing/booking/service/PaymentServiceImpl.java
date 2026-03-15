package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.criteria.PaymentSearchCriteria;
import com.dynamiccarsharing.booking.dto.PaymentDto;
import com.dynamiccarsharing.booking.dto.PaymentRequestDto;
import com.dynamiccarsharing.booking.exception.BookingNotFoundException;
import com.dynamiccarsharing.booking.exception.PaymentNotFoundException;
import com.dynamiccarsharing.booking.filter.PaymentFilter;
import com.dynamiccarsharing.booking.mapper.PaymentMapper;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.Payment;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.repository.PaymentRepository;
import com.dynamiccarsharing.booking.service.interfaces.PaymentService;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.exception.ValidationException;
import com.dynamiccarsharing.util.filter.Filter;
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

    private final BookingRepository bookingRepository;

    @Override
    public PaymentDto createPayment(Long bookingId, PaymentRequestDto requestDto) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException("Booking with ID " + bookingId + " not found."));
        if (booking.getStatus() == TransactionStatus.CANCELED || booking.getStatus() == TransactionStatus.COMPLETED) {
            throw new ValidationException("Payment cannot be created for a canceled or completed booking.");
        }

        requestDto.setBookingId(bookingId);
        Payment payment = paymentMapper.toEntity(requestDto);
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
        payment.setStatus(TransactionStatus.COMPLETED);
        Payment confirmedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(confirmedPayment);
    }

    @Override
    public PaymentDto refundPayment(Long paymentId) {
        Payment payment = getPaymentOrThrow(paymentId);
        if (payment.getStatus() != TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Payment must be COMPLETED to be refunded.");
        }
        payment.setStatus(TransactionStatus.REFUNDED);
        Payment refundedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(refundedPayment);
    }

    private Payment getPaymentOrThrow(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment with ID " + paymentId + " not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentDto> findByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).map(paymentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDto> findPaymentsByStatus(TransactionStatus status) {
        return paymentRepository.findByStatus(status).stream().map(paymentMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDto> searchPayments(PaymentSearchCriteria criteria) {
        Filter<Payment> filter = PaymentFilter.of(
                criteria.getBookingId(),
                criteria.getAmount(),
                criteria.getStatus(),
                criteria.getPaymentMethod()
        );
        try {
            return paymentRepository.findByFilter(filter).stream().map(paymentMapper::toDto).toList();
        } catch (SQLException e) {
            throw new ServiceException("Search for payments failed", e);
        }
    }
}