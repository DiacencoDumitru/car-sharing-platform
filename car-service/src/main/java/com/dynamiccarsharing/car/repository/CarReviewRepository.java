package com.dynamiccarsharing.car.repository;

import com.dynamiccarsharing.car.model.CarReview;
import com.dynamiccarsharing.util.filter.Filter;
import com.dynamiccarsharing.util.repository.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CarReviewRepository extends Repository<CarReview, Long> {

    List<CarReview> findByFilter(Filter<CarReview> filter) throws SQLException;

    List<CarReview> findByCarId(Long carId);

    List<CarReview> findByReviewerId(Long reviewerId);

    Optional<CarReview> findByBookingId(Long bookingId);

    Double averageRatingForCar(Long carId);

    long countRatedByCarId(Long carId);
}