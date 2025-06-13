package com.dynamiccarsharing.carsharing.repository.filter;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Payment;
import lombok.Getter;

@Getter
public class PaymentFilter implements Filter<Payment> {
    private final Long id;
    private final Long bookingId;
    private final Double amount;
    private final TransactionStatus status;
    private final PaymentType paymentMethod;

    private PaymentFilter(Long id, Long bookingId, Double amount, TransactionStatus status, PaymentType paymentMethod) {
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }

    public static PaymentFilter of(Long id, Long bookingId, Double amount, TransactionStatus status, PaymentType paymentMethod) {
        return new PaymentFilter(id, bookingId, amount, status, paymentMethod);
    }

    public static PaymentFilter ofId(Long id) {
        return new PaymentFilter(id, null, null, null, null);
    }

    public static PaymentFilter ofBookingId(Long bookingId) {
        return new PaymentFilter(null, bookingId, null, null, null);
    }

    public static PaymentFilter ofAmount(Double amount) {
        return new PaymentFilter(null, null, amount, null, null);
    }

    public static PaymentFilter ofStatus(TransactionStatus status) {
        return new PaymentFilter(null, null, null, status, null);
    }

    public static PaymentFilter ofPaymentMethod(PaymentType paymentMethod) {
        return new PaymentFilter(null, null, null, null, paymentMethod);
    }

    @Override
    public boolean test(Payment payment) {
        boolean matches = true;
        if (id != null) matches &= payment.getId().equals(id);
        if (bookingId != null) matches &= payment.getBookingId().equals(bookingId);
        if (amount != null) matches &= payment.getAmount() == amount;
        if (status != null) matches &= payment.getStatus() == status;
        if (paymentMethod != null) matches &= payment.getPaymentMethod().equals(paymentMethod);
        return matches;
    }
}
