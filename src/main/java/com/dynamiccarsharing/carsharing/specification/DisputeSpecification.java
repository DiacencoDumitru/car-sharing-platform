package com.dynamiccarsharing.carsharing.specification;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.model.Dispute;
import org.springframework.data.jpa.domain.Specification;

public class DisputeSpecification {

    private DisputeSpecification() {
    }

    public static Specification<Dispute> hasBookingId(Long bookingId) {
        return (root, query, cb) -> bookingId != null ? cb.equal(root.get("booking").get("id"), bookingId) : null;
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