package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Review;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ReviewRepository implements Repository<Review> {

    private final Map<Long, Review> reviewsById = new HashMap<>();
    private final Map<Long, Review> reviewsByReviewerId = new HashMap<>();

    @Override
    public void save(Review review) {
        reviewsById.put(review.getId(), review);
        reviewsByReviewerId.put(review.getReviewerId(), review);
    }

    @Override
    public Review findById(Long id) {
        return reviewsById.get(id);
    }

    @Override
    public Review findByField(String fieldValue) {
        try {
            Long reviewerId = Long.parseLong(fieldValue);
            return reviewsByReviewerId.get(reviewerId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void update(Review review) {
        if (reviewsById.containsKey(review.getId())) {
            reviewsByReviewerId.remove(reviewsById.get(review.getId()).getReviewerId());
            reviewsById.put(review.getId(), review);
            reviewsByReviewerId.put(review.getReviewerId(), review);
        }
    }

    @Override
    public void delete(Long id) {
        Review review = reviewsById.get(id);
        reviewsById.remove(id);
        reviewsByReviewerId.remove(review.getReviewerId());
    }

    @Override
    public Map<Long, Review> findAll() {
        return new HashMap<>(reviewsById);
    }

    public Map<Long, Review> findByFilter(String field, String value) {
        return reviewsById.entrySet().stream()
                .filter(entry -> {
                    Review review = entry.getValue();
                    return (field.equals("type") && review.getType().equals(value));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}