package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.exception.LocationNotFoundException;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.jpa.LocationJpaRepository;
import com.dynamiccarsharing.carsharing.dto.criteria.LocationSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private LocationJpaRepository locationRepository;

    private LocationServiceImpl locationService;

    @BeforeEach
    void setUp() {
        locationService = new LocationServiceImpl(locationRepository);
    }

    private Location createTestLocation(Long id) {
        return Location.builder()
                .id(id)
                .city("New York")
                .state("New York")
                .zipCode("10001")
                .build();
    }

    @Test
    void createLocation_shouldCallRepositoryAndReturnLocation() {
        Location locationToSave = createTestLocation(null);
        Location savedLocation = createTestLocation(1L);
        when(locationRepository.save(locationToSave)).thenReturn(savedLocation);

        Location result = locationService.createLocation(locationToSave);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("New York", result.getCity());
        verify(locationRepository).save(locationToSave);
    }

    @Test
    void findById_whenLocationExists_shouldReturnOptionalOfLocation() {
        Long locationId = 1L;
        Location testLocation = createTestLocation(locationId);

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));

        Optional<Location> result = locationService.findById(locationId);

        assertTrue(result.isPresent());
        assertEquals(locationId, result.get().getId());
        verify(locationRepository).findById(locationId);
    }

    @Test
    void findById_whenLocationDoesNotExist_shouldReturnEmptyOptional() {
        Long locationId = 1L;
        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        Optional<Location> result = locationService.findById(locationId);

        assertFalse(result.isPresent());
    }

    @Test
    void deleteById_whenLocationExists_shouldSucceed() {
        Long locationId = 1L;
        when(locationRepository.findById(locationId)).thenReturn(Optional.of(createTestLocation(locationId)));
        doNothing().when(locationRepository).deleteById(locationId);

        locationService.deleteById(locationId);

        verify(locationRepository).deleteById(locationId);
    }

    @Test
    void deleteById_whenLocationDoesNotExist_shouldThrowLocationNotFoundException() {
        Long locationId = 1L;
        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        assertThrows(LocationNotFoundException.class, () -> locationService.deleteById(locationId));
    }

    @Test
    void searchLocations_withCriteria_shouldCallRepositoryWithSpecification() throws SQLException {
        String city = "New York";
        LocationSearchCriteria criteria = LocationSearchCriteria.builder().city(city).build();
        when(locationRepository.findByFilter(any(Filter.class))).thenReturn(List.of(createTestLocation(1L)));

        List<Location> results = locationService.searchLocations(criteria);

        assertFalse(results.isEmpty());
        verify(locationRepository, times(1)).findByFilter(any(Filter.class));
    }
}