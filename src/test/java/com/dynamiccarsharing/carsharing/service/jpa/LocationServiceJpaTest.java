package com.dynamiccarsharing.carsharing.service.jpa;

import com.dynamiccarsharing.carsharing.exception.LocationNotFoundException;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.jpa.LocationJpaRepository;
import com.dynamiccarsharing.carsharing.dto.LocationSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceJpaTest {

    @Mock
    private LocationJpaRepository locationRepository;

    private LocationServiceJpaImpl locationService;

    @BeforeEach
    void setUp() {
        locationService = new LocationServiceJpaImpl(locationRepository);
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
        when(locationRepository.existsById(locationId)).thenReturn(true);
        doNothing().when(locationRepository).deleteById(locationId);

        locationService.deleteById(locationId);

        verify(locationRepository).deleteById(locationId);
    }

    @Test
    void deleteById_whenLocationDoesNotExist_shouldThrowLocationNotFoundException() {
        Long locationId = 1L;
        when(locationRepository.existsById(locationId)).thenReturn(false);

        assertThrows(LocationNotFoundException.class, () -> locationService.deleteById(locationId));
    }

    @Test
    void searchLocations_withCriteria_shouldCallRepositoryWithSpecification() {
        String city = "New York";
        LocationSearchCriteria criteria = LocationSearchCriteria.builder().city(city).build();
        when(locationRepository.findAll(any(Specification.class))).thenReturn(List.of(createTestLocation(1L)));

        List<Location> results = locationService.searchLocations(criteria);

        assertFalse(results.isEmpty());
        verify(locationRepository, times(1)).findAll(any(Specification.class));
    }
}