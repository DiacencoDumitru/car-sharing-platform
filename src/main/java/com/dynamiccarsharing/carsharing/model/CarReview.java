package com.dynamiccarsharing.carsharing.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.With;

@Getter
@ToString
@EqualsAndHashCode
public class CarReview implements Review {
    private final Long id;
    private final Long reviewerId;
    private final Long carId;
    @With
    private final String comment;

    public CarReview(Long id, Long reviewerId, Long carId, String comment) {
        Review.validateReviewData(id, reviewerId, comment);
        this.id = id;
        this.reviewerId = reviewerId;
        this.carId = carId;
        this.comment = comment;
    }
}
