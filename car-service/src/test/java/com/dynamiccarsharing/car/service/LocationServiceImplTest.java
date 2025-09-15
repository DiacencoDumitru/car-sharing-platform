package com.dynamiccarsharing.car.service;

import com.dynamiccarsharing.car.dto.LocationCreateRequestDto;
import com.dynamiccarsharing.car.dto.LocationDto;
import com.dynamiccarsharing.car.dto.LocationUpdateRequestDto;
import com.dynamiccarsharing.car.exception.LocationNotFoundException;
import com.dynamiccarsharing.car.mapper.LocationMapper;
import com.dynamiccarsharing.car.model.Location;
import com.dynamiccarsharing.car.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private LocationRepository locationRepository;
    @Mock
    private LocationMapper locationMapper;

    private LocationServiceImpl locationService;

    @BeforeEach
    void setUp() {
        locationService = new LocationServiceImpl(
                locationRepository,
                locationMapper
        );
    }

    @Test
    @DisplayName("createLocation() should save and return a DTO")
    void createLocation_shouldSaveAndReturnDto() {
        var createDto = new LocationCreateRequestDto();
        var location = Location.builder().build();
        var savedLocation = Location.builder().id(1L).build();
        var locationDto = new LocationDto();

        when(locationMapper.toEntity(createDto)).thenReturn(location);
        when(locationRepository.save(location)).thenReturn(savedLocation);
        when(locationMapper.toDto(savedLocation)).thenReturn(locationDto);

        LocationDto result = locationService.createLocation(createDto);

        assertNotNull(result);
        verify(locationRepository).save(location);
    }

    @Test
    @DisplayName("findLocationById() should return DTO when location exists")
    void findLocationById_whenExists_shouldReturnDto() {
        Long locationId = 1L;
        var location = Location.builder().id(locationId).build();
        var locationDto = new LocationDto();

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));
        when(locationMapper.toDto(location)).thenReturn(locationDto);

        Optional<LocationDto> result = locationService.findLocationById(locationId);

        assertTrue(result.isPresent());
        assertEquals(locationDto, result.get());
        verify(locationRepository).findById(locationId);
    }

    @Test
    @DisplayName("findAllLocations() should return a list of DTOs")
    void findAllLocations_shouldReturnDtoList() {
        var location = Location.builder().id(1L).build();
        var locationDto = new LocationDto();

        when(locationRepository.findAll()).thenReturn(List.of(location));
        when(locationMapper.toDto(location)).thenReturn(locationDto);

        List<LocationDto> results = locationService.findAllLocations();

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        verify(locationRepository).findAll();
    }

    @Test
    @DisplayName("updateLocation() should update and return DTO when location exists")
    void updateLocation_whenExists_shouldUpdateAndReturnDto() {
        Long locationId = 1L;
        var updateDto = new LocationUpdateRequestDto();
        var existingLocation = Location.builder().id(locationId).build();
        var updatedDto = new LocationDto();

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(existingLocation));
        when(locationRepository.save(existingLocation)).thenReturn(existingLocation);
        when(locationMapper.toDto(existingLocation)).thenReturn(updatedDto);

        LocationDto result = locationService.updateLocation(locationId, updateDto);

        assertNotNull(result);
        verify(locationRepository).save(existingLocation);
    }

    @Test
    @DisplayName("deleteById() should succeed when location exists")
    void deleteById_whenLocationExists_shouldSucceed() {
        Long locationId = 1L;

        when(locationRepository.existsById(locationId)).thenReturn(true);
        doNothing().when(locationRepository).deleteById(locationId);

        locationService.deleteById(locationId);

        verify(locationRepository).deleteById(locationId);
    }

    @Test
    @DisplayName("deleteById() should throw exception when location does not exist")
    void deleteById_whenLocationDoesNotExist_shouldThrowException() {
        Long locationId = 1L;
        when(locationRepository.existsById(locationId)).thenReturn(false);

        assertThrows(LocationNotFoundException.class, () -> {
            locationService.deleteById(locationId);
        });

        verify(locationRepository, never()).deleteById(anyLong());
    }
}