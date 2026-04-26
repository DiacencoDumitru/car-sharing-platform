package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.Payment;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.util.exception.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class PaymentLifecyclePolicy {

    public void ensureBookingAllowsPaymentCreation(Booking booking) {
        if (booking.getStatus() == TransactionStatus.CANCELED || booking.getStatus() == TransactionStatus.COMPLETED) {
            throw new ValidationException("Payment cannot be created for a canceled or completed booking.");
        }
    }

    public void ensureCanConfirm(Payment payment) {
        if (payment.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Payment must be PENDING to be confirmed.");
        }
    }

    public void ensureCanRefund(Payment payment) {
        if (payment.getStatus() != TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Payment must be COMPLETED to be refunded.");
        }
    }
}
