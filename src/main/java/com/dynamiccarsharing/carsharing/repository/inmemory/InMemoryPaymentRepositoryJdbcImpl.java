package com.dynamiccarsharing.carsharing.repository.inmemory;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.jdbc.PaymentRepositoryJdbcImpl;
import com.dynamiccarsharing.carsharing.filter.Filter;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryPaymentRepositoryJdbcImpl implements PaymentRepositoryJdbcImpl {
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
        return paymentMap.values().stream().filter(filter::test).collect(Collectors.toList());
    }

    @Override
    public Iterable<Payment> findAll() {
        return paymentMap.values();
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
                .collect(Collectors.toList());
    }
}