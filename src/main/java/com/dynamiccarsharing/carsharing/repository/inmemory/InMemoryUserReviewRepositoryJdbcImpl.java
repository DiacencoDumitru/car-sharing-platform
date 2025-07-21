package com.dynamiccarsharing.carsharing.repository.inmemory;

import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.jdbc.UserReviewRepositoryJdbcImpl;
import com.dynamiccarsharing.carsharing.filter.Filter;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryUserReviewRepositoryJdbcImpl implements UserReviewRepositoryJdbcImpl {
    private final Map<Long, UserReview> userReviewMap = new HashMap<>();

    @Override
    public UserReview save(UserReview userReview) {
        userReviewMap.put(userReview.getId(), userReview);
        return userReview;
    }

    @Override
    public Optional<UserReview> findById(Long id) {
        return Optional.ofNullable(userReviewMap.get(id));
    }

    @Override
    public void deleteById(Long id) {
        userReviewMap.remove(id);
    }

    @Override
    public List<UserReview> findByFilter(Filter<UserReview> filter) {
        return userReviewMap.values().stream().filter(filter::test).collect(Collectors.toList());
    }

    @Override
    public Iterable<UserReview> findAll() {
        return userReviewMap.values();
    }

    @Override
    public List<UserReview> findByUserId(Long userId) {
        return userReviewMap.values().stream()
                .filter(review -> review.getUser() != null && review.getUser().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserReview> findByReviewerId(Long reviewerId) {
        return userReviewMap.values().stream()
                .filter(review -> review.getReviewer() != null && review.getReviewer().getId().equals(reviewerId))
                .collect(Collectors.toList());
    }
}