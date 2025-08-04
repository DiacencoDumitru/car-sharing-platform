package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.model.Car;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class CarReviewMapperTest {

    private final CarReviewMapper carReviewMapper = Mappers.getMapper(CarReviewMapper.class);

    @Test
    void map_withValidCarId_shouldReturnCarWithId() {
        Long carId = 20L;

        Car result = carReviewMapper.map(carId);

        assertNotNull(result);
        assertEquals(carId, result.getId());
    }

    @Test
    void map_withNullCarId_shouldReturnNull() {
        Car result = carReviewMapper.map(null);

        assertNull(result);
    }
}