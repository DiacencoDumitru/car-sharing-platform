package com.dynamiccarsharing.carsharing.repository.specification;

import com.dynamiccarsharing.carsharing.model.CarReview;
import org.springframework.data.jpa.domain.Specification;
import java.util.UUID;

public class CarReviewSpecification {

    public static Specification<CarReview> hasCarId(UUID carId) {
        return (root, query, cb) -> cb.equal(root.get("car").get("id"), carId);
    }

    public static Specification<CarReview> hasReviewerId(UUID reviewerId) {
        return (root, query, cb) -> cb.equal(root.get("reviewer").get("id"), reviewerId);
    }
}