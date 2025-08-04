package com.dynamiccarsharing.carsharing.filter;

import com.dynamiccarsharing.carsharing.model.CarReview;
import lombok.Getter;

import java.util.Objects;

@Getter
public class CarReviewFilter implements Filter<CarReview> {
    private final Long reviewerId;
    private final Long carId;

    private CarReviewFilter(Long reviewerId, Long carId) {
        this.reviewerId = reviewerId;
        this.carId = carId;
    }

    public static CarReviewFilter of(Long reviewerId, Long carId) {
        return new CarReviewFilter(reviewerId, carId);
    }

    public static CarReviewFilter ofReviewerId(Long reviewerId) {
        return new CarReviewFilter(reviewerId, null);
    }

    public static CarReviewFilter ofCarId(Long carId) {
        return new CarReviewFilter(null, carId);
    }

    @Override
    public boolean test(CarReview review) {
        boolean matches = true;
        if (reviewerId != null) matches &= Objects.equals(review.getReviewer().getId(), reviewerId);
        if (carId != null) matches &= Objects.equals(review.getCar().getId(), carId);
        return matches;
    }
}