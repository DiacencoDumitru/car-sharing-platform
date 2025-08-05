package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.dto.UserReviewCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserReviewDto;
import com.dynamiccarsharing.carsharing.dto.UserReviewUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.criteria.UserReviewSearchCriteria;
import com.dynamiccarsharing.carsharing.model.UserReview;

import java.util.List;
import java.util.Optional;

public interface UserReviewService {
    UserReviewDto createUserReview(Long userId, UserReviewCreateRequestDto createDto);

    Optional<UserReviewDto> findReviewById(Long id);

    List<UserReviewDto> findReviewsByUserId(Long userId);

    UserReviewDto updateReview(Long reviewId, UserReviewUpdateRequestDto updateDto);

    void deleteById(Long id);

    List<UserReview> searchReviews(UserReviewSearchCriteria criteria);
}