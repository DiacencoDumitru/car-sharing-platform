package com.dynamiccarsharing.booking.filter;

import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.model.Transaction;
import com.dynamiccarsharing.util.filter.Filter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

import java.util.Objects;

@Getter
public class TransactionFilter implements Filter<Transaction> {
    private final Long bookingId;
    private final TransactionStatus status;
    private final PaymentType paymentMethod;

    private TransactionFilter(Long bookingId, TransactionStatus status, PaymentType paymentMethod) {
        this.bookingId = bookingId;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }

    public static TransactionFilter of(Long bookingId, TransactionStatus status, PaymentType paymentMethod) {
        return new TransactionFilter(bookingId, status, paymentMethod);
    }

    public static TransactionFilter ofBookingId(Long bookingId) {
        return new TransactionFilter(bookingId, null, null);
    }

    public static TransactionFilter ofStatus(TransactionStatus status) {
        return new TransactionFilter(null, status, null);
    }

    public static TransactionFilter ofPaymentMethod(PaymentType paymentMethod) {
        return new TransactionFilter(null, null, paymentMethod);
    }

    @Override
    @SuppressFBWarnings(value = {"EQ_COMPARE_OBJECT_WITH_STRING", "EC_UNRELATED_TYPES"}, justification = "getId() is GUARANTEED to return a Long in Transaction.class")
    public boolean test(Transaction transaction) {
        boolean matches = true;
        if (bookingId != null) matches &= Objects.equals(transaction.getBooking().getId(), bookingId);
        if (status != null) matches &= transaction.getStatus() == status;
        if (paymentMethod != null) matches &= transaction.getPaymentMethod() == paymentMethod;
        return matches;
    }
}
