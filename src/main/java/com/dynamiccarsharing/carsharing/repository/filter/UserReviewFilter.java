package com.dynamiccarsharing.carsharing.repository.filter;

import com.dynamiccarsharing.carsharing.model.UserReview;

public class UserReviewFilter implements Filter<UserReview> {
    private Long reviewerId;
    private Long targetId;

    public UserReviewFilter setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
        return this;
    }

    public UserReviewFilter setTargetId(Long targetId) {
        this.targetId = targetId;
        return this;
    }

    @Override
    public boolean test(UserReview review) {
        boolean matches = true;
        if (reviewerId != null) matches &= review.getReviewerId().equals(reviewerId);
        if (targetId != null) matches &= review.getTargetId().equals(targetId);
        return matches;
    }
}