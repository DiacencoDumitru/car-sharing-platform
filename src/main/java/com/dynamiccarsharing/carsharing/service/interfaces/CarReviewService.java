package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.dto.CarReviewCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.CarReviewDto;
import com.dynamiccarsharing.carsharing.dto.CarReviewUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.criteria.CarReviewSearchCriteria;
import com.dynamiccarsharing.carsharing.model.CarReview;
<<<<<<< HEAD
=======
import com.dynamiccarsharing.carsharing.dto.criteria.CarReviewSearchCriteria;
>>>>>>> fix/controller-mvc-tests

import java.util.List;
import java.util.Optional;

public interface CarReviewService {

    Optional<CarReviewDto> findById(Long id);

    void deleteById(Long id);

    List<CarReviewDto> findAll();

    List<CarReviewDto> findByCarId(Long carId);

    CarReviewDto createReview(Long carId, CarReviewCreateRequestDto createDto);

    CarReviewDto updateReview(Long reviewId, CarReviewUpdateRequestDto updateDto);

    List<CarReview> searchReviews(CarReviewSearchCriteria criteria);

    List<CarReview> findByCarId(Long carId);

    CarReview updateReviewComment(Long reviewId, String newComment);
}