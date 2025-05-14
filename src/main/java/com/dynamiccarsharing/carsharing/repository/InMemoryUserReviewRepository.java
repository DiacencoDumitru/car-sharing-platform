package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.filter.UserReviewFilter;

import java.util.*;

public class InMemoryUserReviewRepository implements UserReviewRepository {
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
    public Iterable<UserReview> findAll() {
        return userReviewMap.values();
    }

    @Override
    public List<UserReview> findByFilter(UserReviewFilter filter) {
        return userReviewMap.values().stream().filter(filter::test).toList();
    }
}
