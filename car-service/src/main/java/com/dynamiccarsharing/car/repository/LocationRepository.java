package com.dynamiccarsharing.car.repository;

import com.dynamiccarsharing.car.model.Location;
import com.dynamiccarsharing.util.filter.Filter;
import com.dynamiccarsharing.util.repository.Repository;

import java.sql.SQLException;
import java.util.List;

public interface LocationRepository extends Repository<Location, Long> {

    List<Location> findByCityIgnoreCase(String city);

    List<Location> findByStateIgnoreCase(String state);

    List<Location> findByZipCode(String zipCode);

    List<Location> findByFilter(Filter<Location> filter) throws SQLException;
}