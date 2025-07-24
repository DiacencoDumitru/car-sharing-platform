package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.UserReview;

import java.sql.SQLException;
import java.util.List;

public interface UserReviewRepository extends Repository<UserReview, Long> {

    List<UserReview> findByUserId(Long userId);

    List<UserReview> findByReviewerId(Long reviewerId);

    List<UserReview> findByFilter(Filter<UserReview> filter) throws SQLException;

    @Override
    List<UserReview> findAll();
}