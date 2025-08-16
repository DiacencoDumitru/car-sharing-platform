package com.dynamiccarsharing.car.service.interfaces;

import com.dynamiccarsharing.car.criteria.CarReviewSearchCriteria;
import com.dynamiccarsharing.contracts.dto.CarReviewCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.CarReviewDto;
import com.dynamiccarsharing.contracts.dto.CarReviewUpdateRequestDto;
import com.dynamiccarsharing.car.model.CarReview;

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
}