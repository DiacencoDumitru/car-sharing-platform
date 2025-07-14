package com.dynamiccarsharing.carsharing.repository.specification;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class TransactionSpecification {

    public static Specification<Transaction> hasBookingId(UUID bookingId) {
        return (root, query, cb) -> cb.equal(root.get("booking").get("id"), bookingId);
    }

    public static Specification<Transaction> hasStatus(TransactionStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Transaction> hasPaymentMethod(PaymentType paymentMethod) {
        return (root, query, cb) -> cb.equal(root.get("paymentMethod"), paymentMethod);
    }
}