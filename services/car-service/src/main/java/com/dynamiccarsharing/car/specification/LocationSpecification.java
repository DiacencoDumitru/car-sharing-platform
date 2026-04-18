package com.dynamiccarsharing.car.specification;

import com.dynamiccarsharing.car.model.Location;
import org.springframework.data.jpa.domain.Specification;

public class LocationSpecification {

    private LocationSpecification() {
    }

    public static Specification<Location> cityContains(String city) {
        return (root, query, cb) -> city != null ? cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%") : null;
    }

    public static Specification<Location> stateContains(String state) {
        return (root, query, cb) -> state != null ? cb.like(cb.lower(root.get("state")), "%" + state.toLowerCase() + "%") : null;
    }

    public static Specification<Location> hasZipCode(String zipCode) {
        return (root, query, cb) -> zipCode != null ? cb.equal(root.get("zipCode"), zipCode) : null;
    }

    public static Specification<Location> withCriteria(String city, String state, String zipCode) {
        return Specification
                .where(cityContains(city))
                .and(stateContains(state))
                .and(hasZipCode(zipCode));
    }
}