package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Payment;
import com.dynamiccarsharing.carsharing.repository.filter.PaymentFilter;

import java.util.List;

public interface PaymentRepository extends Repository<Payment, Long> {
    List<Payment> findByFilter(PaymentFilter filter);
}