package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.repository.filter.CarFilter;

import java.util.List;

public interface CarRepository extends Repository<Car, Long> {
    List<Car> findByFilter(CarFilter filter);
}
