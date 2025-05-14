package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.repository.filter.CarReviewFilter;

import java.util.List;

public interface CarReviewRepository extends Repository<CarReview, Long> {
    List<CarReview> findByFilter(CarReviewFilter filter);
}
