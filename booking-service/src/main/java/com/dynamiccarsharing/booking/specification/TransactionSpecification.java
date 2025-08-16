package com.dynamiccarsharing.booking.specification;

import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.model.Transaction;
import org.springframework.data.jpa.domain.Specification;

public class TransactionSpecification {

    private TransactionSpecification() {
    }

    public static Specification<Transaction> hasBookingId(Long bookingId) {
        return (root, query, cb) -> bookingId != null ? cb.equal(root.get("booking").get("id"), bookingId) : null;
    }

    public static Specification<Transaction> hasStatus(TransactionStatus status) {
        return (root, query, cb) -> status != null ? cb.equal(root.get("status"), status) : null;
    }

    public static Specification<Transaction> hasPaymentMethod(PaymentType paymentMethod) {
        return (root, query, cb) -> paymentMethod != null ? cb.equal(root.get("paymentMethod"), paymentMethod) : null;
    }

    public static Specification<Transaction> withCriteria(Long bookingId, TransactionStatus status, PaymentType paymentMethod) {
        return Specification.where(hasBookingId(bookingId))
                .and(hasStatus(status))
                .and(hasPaymentMethod(paymentMethod));
    }
}