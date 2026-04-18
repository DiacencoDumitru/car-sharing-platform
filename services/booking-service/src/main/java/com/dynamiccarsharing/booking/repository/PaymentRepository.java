package com.dynamiccarsharing.booking.repository;


import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.model.Payment;
import com.dynamiccarsharing.util.filter.Filter;
import com.dynamiccarsharing.util.repository.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends Repository<Payment, Long> {

    Optional<Payment> findByBookingId(Long bookingId);

    List<Payment> findByStatus(TransactionStatus status);

    List<Payment> findByFilter(Filter<Payment> filter) throws SQLException;
}