package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.InMemoryUserReviewRepository;
import com.dynamiccarsharing.carsharing.repository.filter.UserReviewFilter;
import com.dynamiccarsharing.carsharing.util.Validator;

import java.util.List;
import java.util.Optional;

public class UserReviewService {
    private final InMemoryUserReviewRepository inMemoryUserReviewRepository;

    public UserReviewService(InMemoryUserReviewRepository inMemoryUserReviewRepository) {
        this.inMemoryUserReviewRepository = inMemoryUserReviewRepository;
    }

    public UserReview save(UserReview userReview) {
        Validator.validateNonNull(userReview, "User Review");
        return inMemoryUserReviewRepository.save(userReview);
    }

    public Optional<UserReview> findById(Long id) {
        Validator.validateId(id, "ID");
        return inMemoryUserReviewRepository.findById(id);
    }

    public void delete(Long id) {
        Validator.validateId(id, "ID");
        inMemoryUserReviewRepository.deleteById(id);
    }

    public Iterable<UserReview> findAll() {
        return inMemoryUserReviewRepository.findAll();
    }

    public List<UserReview> findUserReviewsByReviewerId(Long reviewerId) {
        Validator.validateId(reviewerId, "Reviewer ID");
        UserReviewFilter filter = new UserReviewFilter().setReviewerId(reviewerId);
        return (List<UserReview>) inMemoryUserReviewRepository.findByFilter(filter);
    }
}