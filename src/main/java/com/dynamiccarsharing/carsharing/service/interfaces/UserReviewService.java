package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.dto.UserReviewSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface UserReviewService {
    UserReview save(UserReview userReview);

    Optional<UserReview> findById(Long id);

    void deleteById(Long id);

    List<UserReview> findUserReviewsAboutUser(Long userId);

    List<UserReview> searchReviews(UserReviewSearchCriteria criteria);
}