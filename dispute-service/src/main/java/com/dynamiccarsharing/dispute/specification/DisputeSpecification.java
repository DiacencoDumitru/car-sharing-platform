package com.dynamiccarsharing.dispute.specification;

import com.dynamiccarsharing.contracts.enums.DisputeStatus;
import com.dynamiccarsharing.dispute.model.Dispute;
import org.springframework.data.jpa.domain.Specification;

public class DisputeSpecification {

    private DisputeSpecification() {
    }

    public static Specification<Dispute> hasBookingId(Long bookingId) {
        return (root, query, cb) -> bookingId != null ? cb.equal(root.get("bookingId"), bookingId) : null;
    }

    public static Specification<Dispute> hasStatus(DisputeStatus status) {
        return (root, query, cb) -> status != null ? cb.equal(root.get("status"), status) : null;
    }

    public static Specification<Dispute> withCriteria(Long bookingId, DisputeStatus status) {
        return Specification
                .where(hasBookingId(bookingId))
                .and(hasStatus(status));
    }
}