package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class UserReviewMapperTest {

    private final UserReviewMapper userReviewMapper = Mappers.getMapper(UserReviewMapper.class);

    @Test
    void map_withValidUserId_shouldReturnUserWithId() {
        Long userId = 60L;

        User result = userReviewMapper.map(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
    }

    @Test
    void map_withNullUserId_shouldReturnNull() {
        User result = userReviewMapper.map(null);

        assertNull(result);
    }
}