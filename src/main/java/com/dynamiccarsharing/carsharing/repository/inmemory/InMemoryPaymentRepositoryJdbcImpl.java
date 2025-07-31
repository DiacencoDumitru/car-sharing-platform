package com.dynamiccarsharing.carsharing.repository.inmemory;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.PaymentRepository;
import com.dynamiccarsharing.carsharing.filter.Filter;

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
                .toList();
    }
}