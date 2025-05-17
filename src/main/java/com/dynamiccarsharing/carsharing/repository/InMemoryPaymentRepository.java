package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.filter.PaymentFilter;

import java.util.*;

public class InMemoryPaymentRepository implements PaymentRepository {
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
    public Iterable<Payment> findAll() {
        return paymentMap.values();
    }

    public List<Payment> findByFilter(PaymentFilter filter) {
        return paymentMap.values().stream().filter(filter::test).toList();
    }
}
