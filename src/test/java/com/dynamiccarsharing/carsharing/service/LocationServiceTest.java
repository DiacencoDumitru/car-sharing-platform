package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    LocationRepository locationRepository;

    private LocationService locationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reset(locationRepository);
        locationService = new LocationService(locationRepository);
    }

    private Location createTestLocation() {
        return new Location(1L, "New York", "New York", "10001");
    }

    @Test
    void save_shouldCallRepository_shouldReturnSameLocation() {
        Location location = createTestLocation();
        when(locationRepository.save(location)).thenReturn(location);

        Location savedLocation = locationService.save(location);

        verify(locationRepository, times(1)).save(location);
        assertSame(location, savedLocation);
        assertEquals(location.getId(), savedLocation.getId());
        assertEquals(location.getCity(), savedLocation.getCity());
        assertEquals(location.getState(), savedLocation.getState());
        assertEquals(location.getZipCode(), savedLocation.getZipCode());
    }

    @Test
    void save_whenLocationIsNull_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> locationService.save(null));

        assertEquals("Location must be non-null", exception.getMessage());
        verify(locationRepository, never()).save(any());
    }

    @Test
    void findById_whenLocationIsPresent_shouldReturnLocation() {
        Location location = createTestLocation();
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        Optional<Location> foundLocation = locationService.findById(1L);

        verify(locationRepository, times(1)).findById(1L);
        assertTrue(foundLocation.isPresent());
        assertSame(location, foundLocation.get());
        assertEquals(location.getId(), foundLocation.get().getId());
        assertEquals(location.getCity(), foundLocation.get().getCity());
        assertEquals(location.getState(), foundLocation.get().getState());
        assertEquals(location.getZipCode(), foundLocation.get().getZipCode());
    }

    @Test
    void findById_whenLocationNotFound_shouldReturnEmpty() {
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Location> foundLocation = locationService.findById(1L);

        verify(locationRepository, times(1)).findById(1L);
        assertFalse(foundLocation.isPresent());
    }

    @Test
    void findById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> locationService.findById(-1L));

        assertEquals("Location ID must be non-negative", exception.getMessage());
        verify(locationRepository, never()).findById(any());
    }

    @Test
    void deleteById_withValidId_shouldDeleteLocation() {
        locationService.deleteById(1L);

        verify(locationRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> locationService.deleteById(-1L));

        assertEquals("Location ID must be non-negative", exception.getMessage());
        verify(locationRepository, never()).findById(any());
    }

    @Test
    void findAll_withMultipleLocations_shouldReturnAllLocations() {
        Location location1 = createTestLocation();
        Location location2 = new Location(2L, "Chisinau", "Chisinau", "1001");
        List<Location> locations = Arrays.asList(location1, location2);
        when(locationRepository.findAll()).thenReturn(locations);

        Iterable<Location> result = locationService.findAll();

        verify(locationRepository, times(1)).findAll();
        List<Location> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(location1));
        assertTrue(resultList.contains(location2));
        assertEquals(location1.getId(), resultList.get(0).getId());
        assertEquals(location1.getCity(), resultList.get(0).getCity());
        assertEquals(location1.getState(), resultList.get(0).getState());
        assertEquals(location1.getZipCode(), resultList.get(0).getZipCode());
    }

    @Test
    void findAll_withSingleLocation_shouldReturnSingleLocation() {
        Location location = createTestLocation();
        List<Location> locations = Collections.singletonList(location);
        when(locationRepository.findAll()).thenReturn(locations);

        Iterable<Location> result = locationService.findAll();

        verify(locationRepository, times(1)).findAll();
        List<Location> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(1, resultList.size());
        assertSame(location, resultList.get(0));
        assertEquals(location.getId(), resultList.get(0).getId());
        assertEquals(location.getCity(), resultList.get(0).getCity());
        assertEquals(location.getState(), resultList.get(0).getState());
        assertEquals(location.getZipCode(), resultList.get(0).getZipCode());
    }

    @Test
    void findAll_withNoLocations_shouldReturnEmptyIterable() {
        List<Location> locations = Collections.emptyList();
        when(locationRepository.findAll()).thenReturn(locations);

        Iterable<Location> result = locationService.findAll();

        verify(locationRepository, times(1)).findAll();
        List<Location> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertEquals(0, resultList.size());
    }

    @Test
    void findLocationsByCity_withValidCity_shouldReturnLocations() {
        Location location = createTestLocation();
        List<Location> locations = List.of(location);
        when(locationRepository.findByFilter(argThat(filter -> filter != null && filter.test(location) && location.getCity().equals("New York")))).thenReturn(locations);

        List<Location> result = locationService.findLocationsByCity("New York");

        assertEquals(1, result.size());
        assertEquals(location, result.get(0));
        verify(locationRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(location) && location.getCity().equals("New York")));
    }

    @Test
    void findLocationsByState_withValidState_shouldReturnLocations() {
        Location location = createTestLocation();
        List<Location> locations = List.of(location);
        when(locationRepository.findByFilter(argThat(filter -> filter != null && filter.test(location) && location.getState().equals("New York")))).thenReturn(locations);

        List<Location> result = locationService.findLocationsByState("New York");

        assertEquals(1, result.size());
        assertEquals(location, result.get(0));
        verify(locationRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(location) && location.getState().equals("New York")));
    }

    @Test
    void findLocationsByZipCode_withValidZipCode_shouldReturnLocations() {
        Location location = createTestLocation();
        List<Location> locations = List.of(location);
        when(locationRepository.findByFilter(argThat(filter -> filter != null && filter.test(location) && location.getZipCode().equals("10001")))).thenReturn(locations);

        List<Location> result = locationService.findLocationsByZipCode("10001");

        assertEquals(1, result.size());
        assertEquals(location, result.get(0));
        verify(locationRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(location) && location.getZipCode().equals("10001")));
    }
}