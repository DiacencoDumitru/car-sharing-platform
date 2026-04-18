package com.dynamiccarsharing.booking.specification;

import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.booking.model.Booking;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class BookingSpecification {

    private BookingSpecification() {
    }

    public static Specification<Booking> hasRenterId(Long renterId) {
        return (root, query, cb) -> renterId != null ? cb.equal(root.get("renterId"), renterId) : null;
    }

    public static Specification<Booking> hasCarId(Long carId) {
        return (root, query, cb) -> carId != null ? cb.equal(root.get("carId"), carId) : null;
    }

    public static Specification<Booking> hasCarIdIn(List<Long> carIds) {
        return (root, query, cb) -> (carIds != null && !carIds.isEmpty()) ? root.get("carId").in(carIds) : null;
    }

    public static Specification<Booking> hasStatus(TransactionStatus status) {
        return (root, query, cb) -> status != null ? cb.equal(root.get("status"), status) : null;
    }

    public static Specification<Booking> withCriteria(Long renterId, Long carId, List<Long> carIds, TransactionStatus status) {
        Specification<Booking> carSpec = (carIds != null && !carIds.isEmpty()) ? hasCarIdIn(carIds) : hasCarId(carId);
        return Specification.where(hasRenterId(renterId))
                .and(carSpec)
                .and(hasStatus(status));
    }
}