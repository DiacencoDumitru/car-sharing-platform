package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.UserReviewRepository;
import com.dynamiccarsharing.carsharing.repository.filter.UserReviewFilter;
import com.dynamiccarsharing.carsharing.util.Validator;

import java.util.List;
import java.util.Optional;

public class UserReviewService {
    private final UserReviewRepository userReviewRepository;

    public UserReviewService(UserReviewRepository userReviewRepository) {
        this.userReviewRepository = userReviewRepository;
    }

    public UserReview save(UserReview userReview) {
        Validator.validateNonNull(userReview, "UserReview");
        return userReviewRepository.save(userReview);
    }

    public Optional<UserReview> findById(Long id) {
        Validator.validateId(id, "UserReview ID");
        return userReviewRepository.findById(id);
    }

    public void deleteById(Long id) {
        Validator.validateId(id, "UserReview ID");
        userReviewRepository.deleteById(id);
    }

    public Iterable<UserReview> findAll() {
        return userReviewRepository.findAll();
    }

    public List<UserReview> findUserReviewsByReviewerId(Long reviewerId) {
        Validator.validateId(reviewerId, "Reviewer ID");
        UserReviewFilter filter = new UserReviewFilter().setReviewerId(reviewerId);
        return userReviewRepository.findByFilter(filter);
    }
}