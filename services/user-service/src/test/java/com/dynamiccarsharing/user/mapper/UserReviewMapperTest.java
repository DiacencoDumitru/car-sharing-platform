package com.dynamiccarsharing.user.mapper;

import com.dynamiccarsharing.user.model.UserReview;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class UserReviewMapperTest {

    private final UserReviewMapper userReviewMapper = Mappers.getMapper(UserReviewMapper.class);

    @Test
    void map_withValidUserId_shouldReturnUserWithId() {
        Long userId = 60L;

        UserReview result = userReviewMapper.fromId(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
    }

    @Test
    void map_withNullUserId_shouldReturnNull() {
        UserReview result = userReviewMapper.fromId(null);

        assertNull(result);
    }
}