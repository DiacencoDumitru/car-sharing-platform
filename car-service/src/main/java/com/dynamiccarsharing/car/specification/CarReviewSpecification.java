package com.dynamiccarsharing.car.specification;

import com.dynamiccarsharing.car.model.CarReview;
import org.springframework.data.jpa.domain.Specification;

public class CarReviewSpecification {

    private CarReviewSpecification() {
    }

    public static Specification<CarReview> hasCarId(Long carId) {
        return (root, query, cb) -> carId != null ? cb.equal(root.get("car").get("id"), carId) : null;
    }

    public static Specification<CarReview> hasReviewerId(Long reviewerId) {
        return (root, query, cb) -> reviewerId != null ? cb.equal(root.get("reviewerId"), reviewerId) : null;
    }

    public static Specification<CarReview> withCriteria(Long carId, Long reviewerId) {
        return Specification
                .where(hasCarId(carId))
                .and(hasReviewerId(reviewerId));
    }
}