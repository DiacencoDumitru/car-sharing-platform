package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.Location;

import java.sql.SQLException;
import java.util.List;

public interface LocationRepository extends Repository<Location, Long> {

    List<Location> findByCityIgnoreCase(String city);

    List<Location> findByStateIgnoreCase(String state);

    List<Location> findByZipCode(String zipCode);

    List<Location> findByFilter(Filter<Location> filter) throws SQLException;
}