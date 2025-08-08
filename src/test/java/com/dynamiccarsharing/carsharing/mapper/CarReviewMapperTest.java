package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.model.CarReview;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class CarReviewMapperTest {

    private final CarReviewMapper carReviewMapper = Mappers.getMapper(CarReviewMapper.class);

    @Test
    void map_withValidCarId_shouldReturnCarWithId() {
        Long carId = 20L;

        CarReview result = carReviewMapper.fromId(carId);

        assertNotNull(result);
        assertEquals(carId, result.getId());
    }

    @Test
    void map_withNullCarId_shouldReturnNull() {
        CarReview result = carReviewMapper.fromId(null);

        assertNull(result);
    }
}