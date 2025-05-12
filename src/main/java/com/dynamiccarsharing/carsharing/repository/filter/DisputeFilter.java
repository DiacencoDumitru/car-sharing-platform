package com.dynamiccarsharing.carsharing.repository.filter;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.model.Dispute;

public class DisputeFilter implements Filter<Dispute> {
    private Long bookingId;
    private DisputeStatus status;

    public DisputeFilter setBookingId(Long bookingId) {
        this.bookingId = bookingId;
        return this;
    }

    public DisputeFilter setStatus(DisputeStatus status) {
        this.status = status;
        return this;
    }

    @Override
    public boolean test(Dispute dispute) {
        boolean matches = true;
        if (bookingId != null) matches &= dispute.getBookingId().equals(bookingId);
        if (status != null) matches &= dispute.getStatus() == status;
        return matches;
    }
}
