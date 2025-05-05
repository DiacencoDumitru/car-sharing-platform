package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.model.Review;
import com.dynamiccarsharing.carsharing.repository.ReviewRepository;

import java.util.Map;

public class ReviewService {

    private final ReviewRepository reviewRepository = new ReviewRepository();

    public void saveReview(Review review) {
        reviewRepository.save(review);
    }

    public Review findReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    public Review findReviewByReviewerId(Long reviewerId) {
        return reviewRepository.findByField(String.valueOf(reviewerId));
    }

    public void updateReview(Review review) {
        reviewRepository.update(review);
    }

    public void deleteReview(Long id) {
        reviewRepository.delete(id);
    }

    public Map<Long, Review> findAllReviews() {
        return reviewRepository.findAll();
    }

    public Map<Long, Review> findReviewsByFilter(String field, String value) {
        return reviewRepository.findByFilter(field, value);
    }
}
