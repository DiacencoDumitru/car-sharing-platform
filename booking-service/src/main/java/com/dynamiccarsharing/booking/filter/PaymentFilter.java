package com.dynamiccarsharing.booking.filter;

import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.model.Payment;
import com.dynamiccarsharing.util.filter.Filter;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
public class PaymentFilter implements Filter<Payment> {
    private final Long bookingId;
    private final BigDecimal amount;
    private final TransactionStatus status;
    private final PaymentType paymentMethod;

    private PaymentFilter(Long bookingId, BigDecimal amount, TransactionStatus status, PaymentType paymentMethod) {
        this.bookingId = bookingId;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }

    public static PaymentFilter of(Long bookingId, BigDecimal amount, TransactionStatus status, PaymentType paymentMethod) {
        return new PaymentFilter(bookingId, amount, status, paymentMethod);
    }

    public static PaymentFilter ofBookingId(Long bookingId) {
        return new PaymentFilter(bookingId, null, null, null);
    }

    public static PaymentFilter ofAmount(BigDecimal amount) {
        return new PaymentFilter(null, amount, null, null);
    }

    public static PaymentFilter ofStatus(TransactionStatus status) {
        return new PaymentFilter(null, null, status, null);
    }

    public static PaymentFilter ofPaymentMethod(PaymentType paymentMethod) {
        return new PaymentFilter(null, null, null, paymentMethod);
    }

    @Override
    public boolean test(Payment payment) {
        boolean matches = true;
        if (bookingId != null) matches &= Objects.equals(payment.getBooking().getId(), bookingId);
        if (amount != null) matches &= payment.getAmount().equals(amount);
        if (status != null) matches &= payment.getStatus() == status;
        if (paymentMethod != null) matches &= payment.getPaymentMethod().equals(paymentMethod);
        return matches;
    }
}
