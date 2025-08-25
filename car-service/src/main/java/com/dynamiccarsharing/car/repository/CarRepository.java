package com.dynamiccarsharing.car.repository;

import com.dynamiccarsharing.car.criteria.CarSearchCriteria;
import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.util.filter.Filter;
import com.dynamiccarsharing.util.repository.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.SQLException;
import java.util.List;

public interface CarRepository extends Repository<Car, Long> {

    List<Car> findByFilter(Filter<Car> filter) throws SQLException;

    Page<Car> findAll(CarSearchCriteria criteria, Pageable pageable);
}