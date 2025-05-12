package com.dynamiccarsharing.carsharing.repository.filter;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Transaction;

public class TransactionFilter implements Filter<Transaction> {
    private Long bookingId;
    private TransactionStatus status;

    public TransactionFilter setBookingId(Long bookingId) {
        this.bookingId = bookingId;
        return this;
    }

    public TransactionFilter setStatus(TransactionStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public boolean test(Transaction transaction) {
        boolean matches = true;
        if (bookingId != null) matches &= transaction.getTransactionId().equals(bookingId);
        if (status != null) matches &= transaction.getStatus() == status;
        return matches;
    }
}
