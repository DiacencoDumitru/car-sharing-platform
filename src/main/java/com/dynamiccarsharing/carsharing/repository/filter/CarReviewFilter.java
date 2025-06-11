package com.dynamiccarsharing.carsharing.repository.filter;

import com.dynamiccarsharing.carsharing.model.CarReview;
import lombok.Getter;

@Getter
public class CarReviewFilter implements Filter<CarReview> {
    private final Long id;
    private final Long reviewerId;
    private final Long carId;

    private CarReviewFilter(Long id, Long reviewerId, Long carId) {
        this.id = id;
        this.reviewerId = reviewerId;
        this.carId = carId;
    }

    public static CarReviewFilter of(Long id, Long reviewerId, Long carId) {
        return new CarReviewFilter(id, reviewerId, carId);
    }

    public static CarReviewFilter ofId(Long id) {
        return new CarReviewFilter(id, null, null);
    }

    public static CarReviewFilter ofReviewerId(Long reviewerId) {
        return new CarReviewFilter(null, reviewerId, null);
    }

    public static CarReviewFilter ofCarId(Long carId) {
        return new CarReviewFilter(null, null, carId);
    }

    @Override
    public boolean test(CarReview review) {
        boolean matches = true;
        if (reviewerId != null) matches &= review.getReviewerId().equals(reviewerId);
        if (reviewerId != null) matches &= review.getCarId().equals(carId);
        if (id != null) matches &= review.getId().equals(id);
        return matches;
    }
}