package com.dynamiccarsharing.carsharing.repository.filter;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Transaction;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class TransactionFilter implements Filter<Transaction> {
    private Long bookingId;
    private TransactionStatus status;

    public TransactionFilter setStatus(TransactionStatus status) {
        this.status = status;
        return this;
    }

    @Override
    @SuppressFBWarnings(value = {"EQ_COMPARE_OBJECT_WITH_STRING", "EC_UNRELATED_TYPES"}, justification = "getId() is GUARANTEED to return a Long in Transaction.class")
    public boolean test(Transaction transaction) {
        boolean matches = true;
        if (bookingId != null) matches &= transaction.getId().equals(bookingId);
        if (status != null) matches &= transaction.getStatus() == status;
        return matches;
    }
}
