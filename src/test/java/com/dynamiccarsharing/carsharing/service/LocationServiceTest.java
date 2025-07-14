package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.exception.LocationNotFoundException;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    private LocationService locationService;

    @BeforeEach
    void setUp() {
        locationService = new LocationService(locationRepository);
    }

    private Location createTestLocation(UUID id) {
        return Location.builder()
                .id(id)
                .city("New York")
                .state("New York")
                .zipCode("10001")
                .build();
    }

    @Test
    void save_shouldCallRepositoryAndReturnLocation() {
        Location locationToSave = createTestLocation(null);
        Location savedLocation = createTestLocation(UUID.randomUUID());
        when(locationRepository.save(locationToSave)).thenReturn(savedLocation);

        Location result = locationService.save(locationToSave);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("New York", result.getCity());
        verify(locationRepository).save(locationToSave);
    }

    @Test
    void findById_whenLocationExists_shouldReturnOptionalOfLocation() {
        UUID locationId = UUID.randomUUID();
        Location testLocation = createTestLocation(locationId);

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(testLocation));

        Optional<Location> result = locationService.findById(locationId);

        assertTrue(result.isPresent());
        assertEquals(locationId, result.get().getId());
        verify(locationRepository).findById(locationId);
    }

    @Test
    void findById_whenLocationDoesNotExist_shouldReturnEmptyOptional() {
        UUID locationId = UUID.randomUUID();
        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        Optional<Location> result = locationService.findById(locationId);

        assertFalse(result.isPresent());
    }

    @Test
    void deleteById_whenLocationExists_shouldSucceed() {
        UUID locationId = UUID.randomUUID();
        when(locationRepository.existsById(locationId)).thenReturn(true);
        doNothing().when(locationRepository).deleteById(locationId);

        locationService.deleteById(locationId);

        verify(locationRepository).deleteById(locationId);
    }

    @Test
    void deleteById_whenLocationDoesNotExist_shouldThrowLocationNotFoundException() {
        UUID locationId = UUID.randomUUID();
        when(locationRepository.existsById(locationId)).thenReturn(false);

        assertThrows(LocationNotFoundException.class, () -> {
            locationService.deleteById(locationId);
        });
    }

    @Test
    void findAll_shouldReturnListOfLocations() {
        when(locationRepository.findAll()).thenReturn(List.of(createTestLocation(UUID.randomUUID())));

        List<Location> results = locationService.findAll();

        assertEquals(1, results.size());
    }

    @Test
    void findLocationsByCity_shouldCallRepository() {
        String city = "New York";
        when(locationRepository.findByCityIgnoreCase(city)).thenReturn(List.of(createTestLocation(UUID.randomUUID())));

        locationService.findLocationsByCity(city);

        verify(locationRepository).findByCityIgnoreCase(city);
    }

    @Test
    void findLocationsByState_shouldCallRepository() {
        String state = "New York";
        when(locationRepository.findByStateIgnoreCase(state)).thenReturn(List.of(createTestLocation(UUID.randomUUID())));

        locationService.findLocationsByState(state);

        verify(locationRepository).findByStateIgnoreCase(state);
    }

    @Test
    void findLocationsByZipCode_shouldCallRepository() {
        String zipCode = "10001";
        when(locationRepository.findByZipCode(zipCode)).thenReturn(List.of(createTestLocation(UUID.randomUUID())));

        locationService.findLocationsByZipCode(zipCode);

        verify(locationRepository).findByZipCode(zipCode);
    }

    @Test
    void searchLocations_withCriteria_shouldCallRepositoryWithSpecification() {
        String city = "New York";
        when(locationRepository.findAll(any(Specification.class))).thenReturn(List.of(createTestLocation(UUID.randomUUID())));
        List<Location> results = locationService.searchLocations(city, null);

        assertFalse(results.isEmpty());
        verify(locationRepository, times(1)).findAll(any(Specification.class));
    }
}