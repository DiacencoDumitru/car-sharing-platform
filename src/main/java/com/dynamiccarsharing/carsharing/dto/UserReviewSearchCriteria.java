package com.dynamiccarsharing.carsharing.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserReviewSearchCriteria {
    private Long userId;
    private Long reviewerId;
}