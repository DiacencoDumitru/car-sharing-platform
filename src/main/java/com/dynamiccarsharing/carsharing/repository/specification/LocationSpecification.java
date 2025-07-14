package com.dynamiccarsharing.carsharing.repository.specification;

import com.dynamiccarsharing.carsharing.model.Location;
import org.springframework.data.jpa.domain.Specification;

public class LocationSpecification {

    public static Specification<Location> cityContains(String city) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%");
    }

    public static Specification<Location> stateContains(String state) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("state")), "%" + state.toLowerCase() + "%");
    }

    public static Specification<Location> hasZipCode(String zipCode) {
        return (root, query, cb) -> cb.equal(root.get("zipCode"), zipCode);
    }
}