package com.dynamiccarsharing.carsharing.repository.filter;

import com.dynamiccarsharing.carsharing.model.CarReview;

public class CarReviewFilter implements Filter<CarReview> {
    private Long reviewerId;
    private Long targetId;

    public CarReviewFilter setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
        return this;
    }

    public CarReviewFilter setTargetId(Long targetId) {
        this.targetId = targetId;
        return this;
    }

    @Override
    public boolean test(CarReview review) {
        boolean matches = true;
        if (reviewerId != null) matches &= review.getReviewerId().equals(reviewerId);
        if (targetId != null) matches &= review.getTargetId().equals(targetId);
        return matches;
    }
}
