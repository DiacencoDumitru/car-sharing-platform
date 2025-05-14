package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.filter.UserReviewFilter;

import java.util.List;

public interface UserReviewRepository extends Repository<UserReview, Long> {
    List<UserReview> findByFilter(UserReviewFilter filter);
}
