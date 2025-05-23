package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.filter.LocationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryLocationRepositoryTest {

    private InMemoryLocationRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryLocationRepository();
        repository.findAll().forEach(location -> repository.deleteById(location.getId()));
    }

    private Location createTestLocation(Long id, String city) {
        return new Location(id, city, "NY", "10001");
    }

    @Test
    void save_shouldSaveAndReturnLocation() {
        Location location = createTestLocation(1L, "New York");

        Location savedLocation = repository.save(location);

        assertEquals(location, savedLocation);
        assertTrue(repository.findById(1L).isPresent());
        assertEquals(location, repository.findById(1L).get());
    }

    @Test
    void save_withNullLocation_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
    }

    @Test
    void findById_withExistingId_shouldReturnLocation() {
        Location location = createTestLocation(1L, "New York");
        repository.save(location);

        Optional<Location> foundLocation = repository.findById(1L);

        assertTrue(foundLocation.isPresent());
        assertEquals(location, foundLocation.get());
    }

    @Test
    void findById_withNonExistingId_shouldReturnEmpty() {
        Optional<Location> foundLocation = repository.findById(1L);

        assertFalse(foundLocation.isPresent());
    }

    @Test
    void deleteById_withExistingId_shouldRemoveLocation() {
        Location location = createTestLocation(1L, "New York");
        repository.save(location);

        repository.deleteById(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void deleteById_withNonExistingId_shouldDoNothing() {
        repository.deleteById(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void findAll_withMultipleLocations_shouldReturnAllLocations() {
        Location location1 = createTestLocation(1L, "New York");
        Location location2 = createTestLocation(2L, "Chicago");
        repository.save(location1);
        repository.save(location2);

        Iterable<Location> locations = repository.findAll();
        List<Location> locationList = new ArrayList<>();
        locations.forEach(locationList::add);

        assertEquals(2, locationList.size());
        assertTrue(locationList.contains(location1));
        assertTrue(locationList.contains(location2));
    }

    @Test
    void findAll_withSingleLocation_shouldReturnSingleLocation() {
        Location location = createTestLocation(1L, "New York");
        repository.save(location);

        Iterable<Location> locations = repository.findAll();
        List<Location> locationList = new ArrayList<>();
        locations.forEach(locationList::add);

        assertEquals(1, locationList.size());
        assertEquals(location, locationList.get(0));
    }

    @Test
    void findAll_withNoLocations_shouldReturnEmptyIterable() {
        Iterable<Location> locations = repository.findAll();
        List<Location> locationList = new ArrayList<>();
        locations.forEach(locationList::add);

        assertEquals(0, locationList.size());
    }

    @Test
    void findByFilter_withMatchingLocations_shouldReturnMatchingLocations() {
        Location location1 = createTestLocation(1L, "New York");
        Location location2 = createTestLocation(2L, "Chicago");
        Location location3 = createTestLocation(3L, "New York");
        repository.save(location1);
        repository.save(location2);
        repository.save(location3);
        LocationFilter filter = mock(LocationFilter.class);
        when(filter.test(any(Location.class))).thenAnswer(invocation -> {
            Location location = invocation.getArgument(0);
            return "New York".equals(location.getCity());
        });

        List<Location> filteredLocations = repository.findByFilter(filter);

        assertEquals(2, filteredLocations.size());
        assertTrue(filteredLocations.contains(location1));
        assertTrue(filteredLocations.contains(location3));
        assertFalse(filteredLocations.contains(location2));
    }

    @Test
    void findByFilter_withNoMatchingLocations_shouldReturnEmptyList() {
        Location location = createTestLocation(1L, "New York");
        repository.save(location);
        LocationFilter filter = mock(LocationFilter.class);
        when(filter.test(any(Location.class))).thenReturn(false);

        List<Location> filteredLocations = repository.findByFilter(filter);

        assertEquals(0, filteredLocations.size());
    }
}