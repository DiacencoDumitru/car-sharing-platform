package com.dynamiccarsharing.carsharing.specification;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Payment;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class PaymentSpecification {

    private PaymentSpecification() {
    }

    public static Specification<Payment> hasBookingId(Long bookingId) {
        return (root, query, cb) -> bookingId != null ? cb.equal(root.get("booking").get("id"), bookingId) : null;
    }

    public static Specification<Payment> hasAmount(BigDecimal amount) {
        return (root, query, cb) -> amount != null ? cb.equal(root.get("amount"), amount) : null;
    }

    public static Specification<Payment> hasStatus(TransactionStatus status) {
        return (root, query, cb) -> status != null ? cb.equal(root.get("status"), status) : null;
    }

    public static Specification<Payment> hasPaymentMethod(PaymentType paymentMethod) {
        return (root, query, cb) -> paymentMethod != null ? cb.equal(root.get("paymentMethod"), paymentMethod) : null;
    }

    public static Specification<Payment> withCriteria(Long bookingId, BigDecimal amount, TransactionStatus status, PaymentType paymentMethod) {
        return Specification
                .where(hasBookingId(bookingId))
                .and(hasAmount(amount))
                .and(hasStatus(status))
                .and(hasPaymentMethod(paymentMethod));
    }
}