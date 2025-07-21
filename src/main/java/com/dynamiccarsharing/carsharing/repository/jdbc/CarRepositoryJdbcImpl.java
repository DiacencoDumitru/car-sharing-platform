package com.dynamiccarsharing.carsharing.repository.jdbc;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.model.Car;

import java.util.List;

public interface CarRepositoryJdbcImpl extends Repository<Car, Long> {
    List<Car> findByStatus(CarStatus status);
}
