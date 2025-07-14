package com.dynamiccarsharing.carsharing.repository.specification;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Payment;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentSpecification {

    public static Specification<Payment> hasBookingId(UUID bookingId) {
        return (root, query, cb) -> cb.equal(root.get("booking").get("id"), bookingId);
    }

    public static Specification<Payment> hasAmountGreaterThan(BigDecimal amount) {
        return (root, query, cb) -> cb.greaterThan(root.get("amount"), amount);
    }

    public static Specification<Payment> hasStatus(TransactionStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Payment> hasPaymentMethod(PaymentType paymentMethod) {
        return (root, query, cb) -> cb.equal(root.get("paymentMethod"), paymentMethod);
    }
}