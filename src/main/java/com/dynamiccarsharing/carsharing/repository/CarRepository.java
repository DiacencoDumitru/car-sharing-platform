package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.dto.criteria.CarSearchCriteria;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.SQLException;
import java.util.List;

public interface CarRepository extends Repository<Car, Long> {

    List<Car> findByFilter(Filter<Car> filter) throws SQLException;

    Page<Car> findAll(CarSearchCriteria criteria, Pageable pageable);
}