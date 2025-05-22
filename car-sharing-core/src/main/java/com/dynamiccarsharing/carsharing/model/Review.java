package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.util.Validator;

public interface Review {
    Long getId();

    Long getReviewerId();

    String getComment();

    static void validateReviewData(Long id, Long reviewerId, String comment) {
        Validator.validateId(id, "ID");
        Validator.validateId(reviewerId, "Reviewer ID");
        if (comment != null && comment.length() > 1000) {
            throw new IllegalArgumentException("Comment is too long, exceeds 1000 characters");
        }
    }
}
