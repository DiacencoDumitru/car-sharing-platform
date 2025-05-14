package com.dynamiccarsharing.carsharing.repository.filter;

import com.dynamiccarsharing.carsharing.model.UserReview;

public class UserReviewFilter implements Filter<UserReview> {
    private Long reviewerId;
    private Long id;

    public UserReviewFilter setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
        return this;
    }

    public UserReviewFilter setId(Long id) {
        this.id = id;
        return this;
    }

    @Override
    public boolean test(UserReview review) {
        boolean matches = true;
        if (reviewerId != null) matches &= review.getReviewerId().equals(reviewerId);
        if (id != null) matches &= review.getId().equals(id);
        return matches;
    }
}