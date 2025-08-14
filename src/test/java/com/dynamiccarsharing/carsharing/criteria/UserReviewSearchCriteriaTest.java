package com.dynamiccarsharing.carsharing.criteria;

import com.dynamiccarsharing.carsharing.dto.criteria.UserReviewSearchCriteria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserReviewSearchCriteriaTest {

    @Test
    void builder_withAllFields_setsAndGetsAllFields() {
        Long userId = 1L;
        Long reviewerId = 2L;

        UserReviewSearchCriteria criteria = UserReviewSearchCriteria.builder()
                .userId(userId)
                .reviewerId(reviewerId)
                .build();

        assertNotNull(criteria);
        assertEquals(userId, criteria.getUserId());
        assertEquals(reviewerId, criteria.getReviewerId());
    }
}