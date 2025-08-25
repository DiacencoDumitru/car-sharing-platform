package com.dynamiccarsharing.user.repository;

import com.dynamiccarsharing.user.model.UserReview;
import com.dynamiccarsharing.util.filter.Filter;
import com.dynamiccarsharing.util.repository.Repository;

import java.sql.SQLException;
import java.util.List;

public interface UserReviewRepository extends Repository<UserReview, Long> {

    List<UserReview> findByUserId(Long userId);

    List<UserReview> findByReviewerId(Long reviewerId);

    List<UserReview> findByFilter(Filter<UserReview> filter) throws SQLException;

    @Override
    List<UserReview> findAll();
}