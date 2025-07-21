package com.dynamiccarsharing.carsharing.filter;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.model.Dispute;
import lombok.Getter;

import java.util.Objects;

@Getter
public class DisputeFilter implements Filter<Dispute> {
    private final Long bookingId;
    private final DisputeStatus status;

    private DisputeFilter(Long bookingId, DisputeStatus status) {
        this.bookingId = bookingId;
        this.status = status;
    }

    public static DisputeFilter of(Long bookingId, DisputeStatus status) {
        return new DisputeFilter(bookingId, status);
    }

    public static DisputeFilter ofBookingId(Long bookingId) {
        return new DisputeFilter(bookingId, null);
    }

    public static DisputeFilter ofStatus(DisputeStatus status) {
        return new DisputeFilter(null, status);
    }

    @Override
    public boolean test(Dispute dispute) {
        boolean matches = true;
        if (bookingId != null) matches &= Objects.equals(dispute.getBooking().getId(), bookingId);
        if (status != null) matches &= dispute.getStatus() == status;
        return matches;
    }
}
