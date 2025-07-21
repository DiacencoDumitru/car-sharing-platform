package com.dynamiccarsharing.carsharing.repository.jdbc;

import com.dynamiccarsharing.carsharing.model.UserReview;

import java.util.List;

public interface UserReviewRepositoryJdbcImpl extends Repository<UserReview, Long> {
    List<UserReview> findByUserId(Long userId);

    List<UserReview> findByReviewerId(Long reviewerId);
}