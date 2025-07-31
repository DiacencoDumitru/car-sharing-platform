package com.dynamiccarsharing.carsharing.filter;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Transaction;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

import java.util.Objects;

@Getter
public class TransactionFilter implements Filter<Transaction> {
    private final Long id;
    private final Long bookingId;
    private final TransactionStatus status;
    private final PaymentType paymentMethod;

    private TransactionFilter(Long id, Long bookingId, TransactionStatus status, PaymentType paymentMethod) {
        this.id = id;
        this.bookingId = bookingId;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }

    public static TransactionFilter of(Long id, Long bookingId, TransactionStatus status, PaymentType paymentMethod) {
        return new TransactionFilter(id, bookingId, status, paymentMethod);
    }

    public static TransactionFilter ofId(Long id) {
        return new TransactionFilter(id, null, null, null);
    }

    public static TransactionFilter ofBookingId(Long bookingId) {
        return new TransactionFilter(null, bookingId, null, null);
    }

    public static TransactionFilter ofStatus(TransactionStatus status) {
        return new TransactionFilter(null, null, status, null);
    }

    public static TransactionFilter ofPaymentMethod(PaymentType paymentMethod) {
        return new TransactionFilter(null, null, null, paymentMethod);
    }

    @Override
    @SuppressFBWarnings(value = {"EQ_COMPARE_OBJECT_WITH_STRING", "EC_UNRELATED_TYPES"}, justification = "getId() is GUARANTEED to return a Long in Transaction.class")
    public boolean test(Transaction transaction) {
        boolean matches = true;
        if (id != null) matches &= Objects.equals(transaction.getId(), id);
        if (bookingId != null) matches &= Objects.equals(transaction.getBooking().getId(), bookingId);
        if (status != null) matches &= transaction.getStatus() == status;
        if (paymentMethod != null) matches &= transaction.getPaymentMethod() == paymentMethod;
        return matches;
    }
}
