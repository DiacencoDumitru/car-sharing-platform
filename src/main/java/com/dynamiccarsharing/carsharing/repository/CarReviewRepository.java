package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.CarReview;

import java.sql.SQLException;
import java.util.List;

public interface CarReviewRepository extends Repository<CarReview, Long> {

    List<CarReview> findByFilter(Filter<CarReview> filter) throws SQLException;

    List<CarReview> findByCarId(Long carId);

    List<CarReview> findByReviewerId(Long reviewerId);
}