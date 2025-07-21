package com.dynamiccarsharing.carsharing.repository.jdbc;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepositoryJdbcImpl extends Repository<Payment, Long> {
    Optional<Payment> findByBookingId(Long bookingId);

    List<Payment> findByStatus(TransactionStatus status);
}