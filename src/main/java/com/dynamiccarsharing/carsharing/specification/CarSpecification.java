package com.dynamiccarsharing.carsharing.specification;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import org.springframework.data.jpa.domain.Specification;

public class CarSpecification {

    private CarSpecification() {
    }

    public static Specification<Car> hasMake(String make) {
        return (root, query, cb) -> make != null ? cb.equal(root.get("make"), make) : null;
    }

    public static Specification<Car> hasModel(String model) {
        return (root, query, cb) -> model != null ? cb.equal(root.get("model"), model) : null;
    }

    public static Specification<Car> hasStatus(CarStatus status) {
        return (root, query, cb) -> status != null ? cb.equal(root.get("status"), status) : null;
    }

    public static Specification<Car> hasLocationId(Long locationId) {
        return (root, query, cb) -> locationId != null ? cb.equal(root.get("location").get("id"), locationId) : null;
    }

    public static Specification<Car> hasType(CarType type) {
        return (root, query, cb) -> type != null ? cb.equal(root.get("type"), type) : null;
    }

    public static Specification<Car> hasVerificationStatus(VerificationStatus status) {
        return (root, query, cb) -> status != null ? cb.equal(root.get("verificationStatus"), status) : null;
    }

    public static Specification<Car> withCriteria(String make, String model, CarStatus status, Long locationId, CarType type, VerificationStatus verificationStatus) {
        return Specification
                .where(hasMake(make))
                .and(hasModel(model))
                .and(hasStatus(status))
                .and(hasLocationId(locationId))
                .and(hasType(type))
                .and(hasVerificationStatus(verificationStatus));
    }
}