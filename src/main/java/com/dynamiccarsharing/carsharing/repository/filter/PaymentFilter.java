package com.dynamiccarsharing.carsharing.repository.filter;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Payment;

public class PaymentFilter implements Filter<Payment> {
    private Long bookingId;
    private TransactionStatus status;

    public PaymentFilter setBookingId(Long bookingId) {
        this.bookingId = bookingId;
        return this;
    }

    public PaymentFilter setStatus(TransactionStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public boolean test(Payment payment) {
        boolean matches = true;
        if (bookingId != null) matches &= payment.getBookingId().equals(bookingId);
        if (status != null) matches &= payment.getStatus() == status;
        return matches;
    }
}
