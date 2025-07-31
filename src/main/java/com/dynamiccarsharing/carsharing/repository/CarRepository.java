package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.Car;

import java.sql.SQLException;
import java.util.List;

public interface CarRepository extends Repository<Car, Long> {

    List<Car> findByFilter(Filter<Car> filter) throws SQLException;
}