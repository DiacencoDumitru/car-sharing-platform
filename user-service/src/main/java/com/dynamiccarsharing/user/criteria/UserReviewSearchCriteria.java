package com.dynamiccarsharing.user.criteria;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserReviewSearchCriteria {
    private Long userId;
    private Long reviewerId;
}