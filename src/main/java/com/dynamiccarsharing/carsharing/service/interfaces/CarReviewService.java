package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.dto.criteria.CarReviewSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface CarReviewService {
    CarReview save(CarReview carReview);

    Optional<CarReview> findById(Long id);

    void deleteById(Long id);

    List<CarReview> findAll();

    List<CarReview> searchReviews(CarReviewSearchCriteria criteria);

    List<CarReview> findByCarId(Long carId);

    CarReview updateReviewComment(Long reviewId, String newComment);
}