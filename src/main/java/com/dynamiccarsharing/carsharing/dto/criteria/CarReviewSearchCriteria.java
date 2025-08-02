package com.dynamiccarsharing.carsharing.dto.criteria;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CarReviewSearchCriteria {
    private Long carId;
    private Long reviewerId;
}