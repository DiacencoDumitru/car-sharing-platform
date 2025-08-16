package com.dynamiccarsharing.car.mapper;

import com.dynamiccarsharing.contracts.dto.LocationCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.LocationDto;
import com.dynamiccarsharing.contracts.dto.LocationUpdateRequestDto;
import com.dynamiccarsharing.car.model.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LocationMapperImpl.class})
class LocationMapperTest {

    @Autowired
    private LocationMapper locationMapper;

    @Test
    @DisplayName("Should correctly map Location entity to LocationDto")
    void toDto_ShouldMapLocationToLocationDto() {
        Location location = new Location(1L, "New York", "NY", "10001");

        LocationDto dto = locationMapper.toDto(location);

        assertNotNull(dto);
        assertEquals(location.getId(), dto.getId());
        assertEquals(location.getCity(), dto.getCity());
        assertEquals(location.getState(), dto.getState());
        assertEquals(location.getZipCode(), dto.getZipCode());
    }

    @Test
    @DisplayName("Should correctly map LocationCreateRequestDto to Location entity")
    void toEntity_ShouldMapCreateDtoToLocation() {
        LocationCreateRequestDto dto = new LocationCreateRequestDto();
        dto.setCity("Chicago");
        dto.setState("IL");
        dto.setZipCode("60601");

        Location location = locationMapper.toEntity(dto);

        assertNotNull(location);
        assertNull(location.getId());
        assertEquals(dto.getCity(), location.getCity());
        assertEquals(dto.getState(), location.getState());
        assertEquals(dto.getZipCode(), location.getZipCode());
    }

    @Test
    @DisplayName("Should update an existing Location from a LocationUpdateRequestDto")
    void updateFromDto_ShouldUpdateAllFields() {
        Location locationToUpdate = new Location(5L, "Los Angeles", "CA", "90001");

        LocationUpdateRequestDto dto = new LocationUpdateRequestDto();
        dto.setCity("San Francisco");
        dto.setState("CA");
        dto.setZipCode("94102");

        locationMapper.updateFromDto(dto, locationToUpdate);

        assertEquals(5L, locationToUpdate.getId());
        assertEquals("San Francisco", locationToUpdate.getCity());
        assertEquals("CA", locationToUpdate.getState());
        assertEquals("94102", locationToUpdate.getZipCode());
    }
    
    @Test
    @DisplayName("Should ignore null fields when updating from DTO")
    void updateFromDto_ShouldIgnoreNullFields() {
        Location locationToUpdate = new Location(10L, "Miami", "FL", "33101");

        LocationUpdateRequestDto dtoWithNulls = new LocationUpdateRequestDto();
        dtoWithNulls.setCity("Orlando");
        dtoWithNulls.setState(null);

        locationMapper.updateFromDto(dtoWithNulls, locationToUpdate);

        assertEquals(10L, locationToUpdate.getId());
        assertEquals("Orlando", locationToUpdate.getCity());
        assertEquals("FL", locationToUpdate.getState());
        assertEquals("33101", locationToUpdate.getZipCode());
    }


    @Test
    @DisplayName("fromId should return Location with ID when ID is not null")
    void fromId_WithNonNullId_ShouldReturnLocationWithId() {
        Long locationId = 1L;

        Location location = locationMapper.fromId(locationId);

        assertNotNull(location);
        assertEquals(locationId, location.getId());
        assertNull(location.getCity());
    }

    @Test
    @DisplayName("fromId should return null when ID is null")
    void fromId_WithNullId_ShouldReturnNull() {
        Location location = locationMapper.fromId(null);

        assertNull(location);
    }
}