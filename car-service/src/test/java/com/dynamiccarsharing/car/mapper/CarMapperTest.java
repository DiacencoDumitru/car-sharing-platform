package com.dynamiccarsharing.car.mapper;

import com.dynamiccarsharing.contracts.dto.CarCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.CarType;
import com.dynamiccarsharing.contracts.enums.VerificationStatus;
import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.model.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CarMapperImpl.class, LocationMapperImpl.class})
class CarMapperTest {

    @Autowired
    private CarMapper carMapper;

    @Test
    @DisplayName("Should correctly map Car entity to CarDto")
    void toDto_ShouldMapCarToCarDto() {
        Location location = Location.builder().id(1L).build();
        Car car = Car.builder()
                .id(10L)
                .registrationNumber("TEST-123")
                .make("Toyota")
                .model("Camry")
                .status(CarStatus.AVAILABLE)
                .location(location)
                .price(new BigDecimal("55.00"))
                .type(CarType.SEDAN)
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();

        CarDto dto = carMapper.toDto(car);

        assertNotNull(dto);
        assertEquals(car.getId(), dto.getId());
        assertEquals(car.getRegistrationNumber(), dto.getRegistrationNumber());
        assertEquals(car.getMake(), dto.getMake());
        assertEquals(car.getModel(), dto.getModel());
        assertEquals(car.getStatus(), dto.getStatus());
        assertEquals(car.getLocation().getId(), dto.getLocationId());
        assertEquals(0, car.getPrice().compareTo(dto.getPrice()));
        assertEquals(car.getType(), dto.getType());
        assertEquals(car.getVerificationStatus(), dto.getVerificationStatus());
    }

    @Test
    @DisplayName("Should correctly map CarCreateRequestDto to Car entity with default statuses")
    void toEntity_ShouldMapCreateDtoToCar() {
        CarCreateRequestDto dto = new CarCreateRequestDto();
        dto.setRegistrationNumber("NEW-456");
        dto.setMake("Honda");
        dto.setModel("Civic");
        dto.setLocationId(2L);
        dto.setPrice(new BigDecimal("45.50"));
        dto.setType(CarType.HATCHBACK);

        Car car = carMapper.toEntity(dto);

        assertNotNull(car);
        assertNull(car.getId());
        assertEquals(dto.getRegistrationNumber(), car.getRegistrationNumber());
        assertEquals(dto.getMake(), car.getMake());
        assertEquals(dto.getModel(), car.getModel());
        assertEquals(dto.getPrice(), car.getPrice());
        assertEquals(dto.getType(), car.getType());
        assertNotNull(car.getLocation());
        assertEquals(dto.getLocationId(), car.getLocation().getId());
        assertEquals(CarStatus.AVAILABLE, car.getStatus());
        assertEquals(VerificationStatus.PENDING, car.getVerificationStatus());
    }

    @Test
    @DisplayName("Should update an existing Car from a CarUpdateRequestDto")
    void updateCarFromDto_ShouldUpdateCarFromUpdateDto() {
        Location originalLocation = Location.builder().id(1L).build();
        Car carToUpdate = Car.builder()
                .id(10L)
                .registrationNumber("OLD-REG")
                .make("Ford")
                .model("Focus")
                .status(CarStatus.RENTED)
                .location(originalLocation)
                .price(new BigDecimal("100.00"))
                .type(CarType.SEDAN)
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();

        Location newLocation = Location.builder().id(2L).build();
        CarUpdateRequestDto updateDto = new CarUpdateRequestDto();
        updateDto.setMake("Tesla");
        updateDto.setModel("Model 3");
        updateDto.setLocationId(newLocation.getId());

        carMapper.updateCarFromDto(updateDto, carToUpdate);

        assertEquals("Tesla", carToUpdate.getMake());
        assertEquals("Model 3", carToUpdate.getModel());
        assertEquals(newLocation.getId(), carToUpdate.getLocation().getId()); // Was updated
        assertEquals(0, new BigDecimal("100.00").compareTo(carToUpdate.getPrice()));
        assertEquals(CarStatus.RENTED, carToUpdate.getStatus());
        assertEquals(CarType.SEDAN, carToUpdate.getType());
        assertEquals(VerificationStatus.VERIFIED, carToUpdate.getVerificationStatus());
        assertEquals(10L, carToUpdate.getId());
    }

    @Test
    @DisplayName("fromId should return Car with ID when ID is not null")
    void fromId_WithNonNullId_ShouldReturnCarWithIdSet() {
        Long carId = 1L;

        Car car = carMapper.fromId(carId);

        assertNotNull(car);
        assertEquals(carId, car.getId());
    }

    @Test
    @DisplayName("fromId should return null when ID is null")
    void fromId_WithNullId_ShouldReturnNull() {
        Car car = carMapper.fromId(null);

        assertNull(car);
    }
}