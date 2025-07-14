package com.dynamiccarsharing.carsharing.repository.specification;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class BookingSpecification {

    public static Specification<Booking> hasRenterId(UUID renterId) {
        return ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("renter").get("id"), renterId));
    }

    public static Specification<Booking> hasCarId(UUID carId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("car").get("id"), carId);
    }

    public static Specification<Booking> hasStatus(TransactionStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }
}
