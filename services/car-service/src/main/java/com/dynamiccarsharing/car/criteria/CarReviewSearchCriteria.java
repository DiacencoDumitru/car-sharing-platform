package com.dynamiccarsharing.car.criteria;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CarReviewSearchCriteria {
    private Long carId;
    private Long reviewerId;
}