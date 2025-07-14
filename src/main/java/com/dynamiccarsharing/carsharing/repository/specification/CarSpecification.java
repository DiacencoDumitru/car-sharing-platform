package com.dynamiccarsharing.carsharing.repository.specification;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class CarSpecification {

    public static Specification<Car> hasMake(String make) {
        return (root, query, cb) -> cb.equal(root.get("make"), make);
    }

    public static Specification<Car> hasModel(String model) {
        return (root, query, cb) -> cb.equal(root.get("model"), model);
    }
    
    public static Specification<Car> hasStatus(CarStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
    
    public static Specification<Car> hasLocationId(UUID locationId) {
        return (root, query, cb) -> cb.equal(root.get("location").get("id"), locationId);
    }
    
    public static Specification<Car> hasType(CarType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }
    
    public static Specification<Car> hasVerificationStatus(VerificationStatus status) {
        return (root, query, cb) -> cb.equal(root.get("verificationStatus"), status);
    }
}