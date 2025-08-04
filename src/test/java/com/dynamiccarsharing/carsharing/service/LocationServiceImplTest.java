package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.LocationCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.LocationDto;
import com.dynamiccarsharing.carsharing.dto.LocationUpdateRequestDto;
import com.dynamiccarsharing.carsharing.exception.LocationNotFoundException;
import com.dynamiccarsharing.carsharing.mapper.LocationMapper;
import com.dynamiccarsharing.carsharing.model.Location;
<<<<<<< HEAD
import com.dynamiccarsharing.carsharing.repository.LocationRepository;
=======
import com.dynamiccarsharing.carsharing.repository.jpa.LocationJpaRepository;
import com.dynamiccarsharing.carsharing.dto.criteria.LocationSearchCriteria;
>>>>>>> fix/controller-mvc-tests
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
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
        locationService = new LocationServiceImpl(locationRepository, locationMapper);
    }

    @Test
    void createLocation_shouldMapAndSaveAndReturnDto() {
        LocationCreateRequestDto createDto = new LocationCreateRequestDto();
        Location locationEntity = Location.builder().build();
        Location savedEntity = Location.builder().id(1L).build();
        LocationDto expectedDto = new LocationDto();
        expectedDto.setId(1L);

        when(locationMapper.toEntity(createDto)).thenReturn(locationEntity);
        when(locationRepository.save(locationEntity)).thenReturn(savedEntity);
        when(locationMapper.toDto(savedEntity)).thenReturn(expectedDto);

        LocationDto result = locationService.createLocation(createDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findLocationById_whenLocationExists_shouldReturnOptionalOfDto() {
        Long locationId = 1L;
        Location locationEntity = Location.builder().id(locationId).build();
        LocationDto expectedDto = new LocationDto();
        expectedDto.setId(locationId);

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(locationEntity));
        when(locationMapper.toDto(locationEntity)).thenReturn(expectedDto);

        Optional<LocationDto> result = locationService.findLocationById(locationId);

        assertTrue(result.isPresent());
        assertEquals(locationId, result.get().getId());
    }

    @Test
    void findAllLocations_shouldMapAndReturnDtoList() {
        Location locationEntity = Location.builder().id(1L).build();
        when(locationRepository.findAll()).thenReturn(Collections.singletonList(locationEntity));
        when(locationMapper.toDto(locationEntity)).thenReturn(new LocationDto());

        List<LocationDto> result = locationService.findAllLocations();

        assertEquals(1, result.size());
    }

    @Test
    void updateLocation_whenExists_shouldUpdateAndReturnDto() {
        Long id = 1L;
        LocationUpdateRequestDto updateDto = new LocationUpdateRequestDto();
        Location existingEntity = Location.builder().id(id).build();

        when(locationRepository.findById(id)).thenReturn(Optional.of(existingEntity));
        when(locationRepository.save(existingEntity)).thenReturn(existingEntity);
        when(locationMapper.toDto(existingEntity)).thenReturn(new LocationDto());

        locationService.updateLocation(id, updateDto);

        verify(locationMapper).updateFromDto(updateDto, existingEntity);
        verify(locationRepository).save(existingEntity);
    }

    @Test
    void updateLocation_whenNotExists_shouldThrowException() {
        Long id = 1L;
        LocationUpdateRequestDto updateDto = new LocationUpdateRequestDto();
        when(locationRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(LocationNotFoundException.class, () -> locationService.updateLocation(id, updateDto));
    }


    @Test
    void deleteById_whenLocationExists_shouldSucceed() {
        Long locationId = 1L;
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(Location.builder().build()));
        doNothing().when(locationRepository).deleteById(locationId);

        locationService.deleteById(locationId);

        verify(locationRepository).deleteById(locationId);
    }

    @Test
    void deleteById_whenLocationDoesNotExist_shouldThrowException() {
        Long locationId = 1L;
        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        assertThrows(LocationNotFoundException.class, () -> locationService.deleteById(locationId));
    }
}