package com.dynamiccarsharing.booking.repository.inmemory;

import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.model.Payment;
import com.dynamiccarsharing.booking.repository.PaymentRepository;
import com.dynamiccarsharing.util.filter.Filter;

import java.util.*;

public class InMemoryPaymentRepositoryJdbcImpl implements PaymentRepository {
    private final Map<Long, Payment> paymentMap = new HashMap<>();

    @Override
    public Payment save(Payment payment) {
        paymentMap.put(payment.getId(), payment);
        return payment;
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return Optional.ofNullable(paymentMap.get(id));
    }

    @Override
    public void deleteById(Long id) {
        paymentMap.remove(id);
    }

    @Override
    public List<Payment> findByFilter(Filter<Payment> filter) {
        return paymentMap.values().stream().filter(filter::test).toList();
    }

    @Override
    public List<Payment> findAll() {
        return new ArrayList<>(paymentMap.values());
    }

    @Override
    public Optional<Payment> findByBookingId(Long bookingId) {
        return paymentMap.values().stream()
                .filter(payment -> payment.getBooking() != null && payment.getBooking().getId().equals(bookingId))
                .findFirst();
    }

    @Override
    public List<Payment> findByStatus(TransactionStatus status) {
        return paymentMap.values().stream()
                .filter(payment -> payment.getStatus() == status)
                .toList();
    }
}