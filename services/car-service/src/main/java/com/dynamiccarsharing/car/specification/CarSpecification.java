package com.dynamiccarsharing.car.specification;

import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.CarType;
import com.dynamiccarsharing.contracts.enums.VerificationStatus;
import com.dynamiccarsharing.car.model.Car;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

public class CarSpecification {

    private CarSpecification() {
    }

    public static Specification<Car> hasMake(String make) {
        return (root, query, cb) -> make != null ? cb.equal(root.get("make"), make) : null;
    }

    public static Specification<Car> hasModel(String model) {
        return (root, query, cb) -> model != null ? cb.equal(root.get("model"), model) : null;
    }

    public static Specification<Car> hasStatusIn(List<CarStatus> statuses) {
        return (root, query, cb) -> statuses != null && !statuses.isEmpty() ? root.get("status").in(statuses) : null;
    }

    public static Specification<Car> hasLocationId(Long locationId) {
        return (root, query, cb) -> locationId != null ? cb.equal(root.get("location").get("id"), locationId) : null;
    }

    public static Specification<Car> hasType(CarType type) {
        return (root, query, cb) -> type != null ? cb.equal(root.get("type"), type) : null;
    }

    public static Specification<Car> priceGreaterThan(BigDecimal price) {
        return (root, query, cb) -> price != null ? cb.greaterThanOrEqualTo(root.get("price"), price) : null;
    }

    public static Specification<Car> priceLessThan(BigDecimal price) {
        return (root, query, cb) -> price != null ? cb.lessThanOrEqualTo(root.get("price"), price) : null;
    }

    public static Specification<Car> hasVerificationStatus(VerificationStatus status) {
        return (root, query, cb) -> status != null ? cb.equal(root.get("verificationStatus"), status) : null;
    }

    public static Specification<Car> hasOwnerId(Long ownerId) {
        return (root, query, cb) -> ownerId != null ? cb.equal(root.get("ownerId"), ownerId) : null;
    }

    public static Specification<Car> hasMinAverageRating(BigDecimal min) {
        return (root, query, cb) -> min == null
                ? null
                : cb.and(
                        cb.isNotNull(root.get("averageRating")),
                        cb.greaterThanOrEqualTo(root.get("averageRating"), min));
    }

    public static Specification<Car> hasMinReviewCount(Integer min) {
        return (root, query, cb) -> min == null
                ? null
                : cb.greaterThanOrEqualTo(
                        cb.coalesce(root.get("reviewCount"), cb.literal(0)),
                        min);
    }

    public static Specification<Car> withCriteria(String make, String model, List<CarStatus> statusIn, Long locationId, CarType type, BigDecimal priceGreaterThan, BigDecimal priceLessThan, VerificationStatus verificationStatus, Long ownerId, BigDecimal minAverageRating, Integer minReviewCount) {
        return Specification
                .where(hasMake(make))
                .and(hasModel(model))
                .and(hasStatusIn(statusIn))
                .and(hasLocationId(locationId))
                .and(hasType(type))
                .and(priceGreaterThan(priceGreaterThan))
                .and(priceLessThan(priceLessThan))
                .and(hasVerificationStatus(verificationStatus))
                .and(hasOwnerId(ownerId))
                .and(hasMinAverageRating(minAverageRating))
                .and(hasMinReviewCount(minReviewCount));
    }
}