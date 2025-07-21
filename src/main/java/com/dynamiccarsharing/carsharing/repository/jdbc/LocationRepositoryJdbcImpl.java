package com.dynamiccarsharing.carsharing.repository.jdbc;

import com.dynamiccarsharing.carsharing.model.Location;

import java.util.List;
import java.util.Optional;

public interface LocationRepositoryJdbcImpl extends Repository<Location, Long> {
    List<Location> findByStateIgnoreCase(String state);

    Optional<Location> findByZipCode(String zipCode);
}