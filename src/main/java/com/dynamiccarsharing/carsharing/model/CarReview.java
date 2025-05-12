package com.dynamiccarsharing.carsharing.model;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class CarReview implements Review {
    private final Long id;
    private final Long reviewerId;
    private final Long targetId;
    private final String comment;

    public CarReview(Long id, Long reviewerId, Long targetId, String comment) {
        Review.validateReviewData(id, reviewerId, targetId, comment);
        this.id = id;
        this.reviewerId = reviewerId;
        this.targetId = targetId;
        this.comment = comment;
    }
}
