package com.dynamiccarsharing.carsharing.repository.specification;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.model.Dispute;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class DisputeSpecification {

    public static Specification<Dispute> hasBookingId(UUID bookingId) {
        return (root, query, cb) -> cb.equal(root.get("booking").get("id"), bookingId);
    }

    public static Specification<Dispute> hasStatus(DisputeStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}