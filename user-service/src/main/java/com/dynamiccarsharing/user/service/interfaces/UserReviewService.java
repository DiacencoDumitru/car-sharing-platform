package com.dynamiccarsharing.user.service.interfaces;


import com.dynamiccarsharing.user.criteria.UserReviewSearchCriteria;
import com.dynamiccarsharing.contracts.dto.UserReviewCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.UserReviewDto;
import com.dynamiccarsharing.contracts.dto.UserReviewUpdateRequestDto;
import com.dynamiccarsharing.user.model.UserReview;

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