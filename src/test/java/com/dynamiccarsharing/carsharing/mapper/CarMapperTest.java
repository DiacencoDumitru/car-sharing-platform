package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.dto.CarCreateRequestDto;
import com.dynamiccarsharing.carsharing.model.Car;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class CarMapperTest {

    @Autowired
    private CarMapper carMapper;

    @Test
    void fromId_withValidId_shouldReturnCarWithId() {
        Long carId = 10L;

        Car result = carMapper.fromId(carId);

        assertNotNull(result);
        assertEquals(carId, result.getId());
    }

    @Test
    void toEntity_shouldMapLocationIdToLocationObject() {
        CarCreateRequestDto dto = new CarCreateRequestDto();
        dto.setLocationId(5L);
        dto.setMake("Toyota");
        dto.setModel("Camry");

        Car result = carMapper.toEntity(dto);

        assertNotNull(result);
        assertNotNull(result.getLocation());
        assertEquals(5L, result.getLocation().getId());
    }
}