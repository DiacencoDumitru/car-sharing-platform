package com.dynamiccarsharing.carsharing.repository.inmemory;

import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.UserReviewRepository;
import com.dynamiccarsharing.carsharing.filter.Filter;

import java.util.*;

public class InMemoryUserReviewRepositoryJdbcImpl implements UserReviewRepository {
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
        return userReviewMap.values().stream().filter(filter::test).toList();
    }

    @Override
    public List<UserReview> findAll() {
        return new ArrayList<>(userReviewMap.values());
    }

    @Override
    public List<UserReview> findByUserId(Long userId) {
        return userReviewMap.values().stream()
                .filter(review -> review.getUser() != null && review.getUser().getId().equals(userId))
                .toList();
    }

    @Override
    public List<UserReview> findByReviewerId(Long reviewerId) {
        return userReviewMap.values().stream()
                .filter(review -> review.getReviewer() != null && review.getReviewer().getId().equals(reviewerId))
                .toList();
    }
}