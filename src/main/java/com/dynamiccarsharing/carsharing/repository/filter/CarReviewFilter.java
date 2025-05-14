package com.dynamiccarsharing.carsharing.repository.filter;

import com.dynamiccarsharing.carsharing.model.CarReview;

public class CarReviewFilter implements Filter<CarReview> {
    private Long id;
    private Long reviewerId;

    public CarReviewFilter setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
        return this;
    }

    public CarReviewFilter setId(Long id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean test(CarReview review) {
        boolean matches = true;
        if (reviewerId != null) matches &= review.getReviewerId().equals(reviewerId);
        if (id != null) matches &= review.getId().equals(id);
        return matches;
    }
}