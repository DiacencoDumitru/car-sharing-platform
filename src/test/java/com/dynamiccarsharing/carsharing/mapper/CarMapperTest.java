package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.model.Location;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class CarMapperTest {

    private final CarMapper carMapper = Mappers.getMapper(CarMapper.class);

    @Test
    void toLocation_withValidId_shouldReturnLocationWithId() {
        Long locationId = 10L;

        Location result = carMapper.toLocation(locationId);

        assertNotNull(result);
        assertEquals(locationId, result.getId());
    }

    @Test
    void toLocation_withNullId_shouldReturnNull() {
        Location result = carMapper.toLocation(null);

        assertNull(result);
    }
}