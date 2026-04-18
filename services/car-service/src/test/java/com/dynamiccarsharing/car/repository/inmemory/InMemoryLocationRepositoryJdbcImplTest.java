package com.dynamiccarsharing.car.repository.inmemory;

import com.dynamiccarsharing.car.filter.LocationFilter;
import com.dynamiccarsharing.car.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryLocationRepositoryJdbcImplTest {

    private InMemoryLocationRepositoryJdbcImpl repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryLocationRepositoryJdbcImpl();
    }

    private Location createTestLocation(Long id, String city, String state, String zipCode) {
        return Location.builder()
                .id(id)
                .city(city)
                .state(state)
                .zipCode(zipCode)
                .build();
    }

    @Nested
    @DisplayName("CRUD and FindAll Operations")
    class CrudTests {
        @Test
        void save_shouldSaveAndReturnLocation() {
            Location location = createTestLocation(1L, "New York", "NY", "10001");
            Location savedLocation = repository.save(location);
            assertEquals(location, savedLocation);
            assertTrue(repository.findById(1L).isPresent());
        }

        @Test
        void save_updateExistingLocation_shouldChangeCity() {
            Location original = createTestLocation(1L, "New York", "NY", "10001");
            repository.save(original);

            original.setCity("NYC");
            repository.save(original);

            Optional<Location> found = repository.findById(1L);
            assertTrue(found.isPresent());
            assertEquals("NYC", found.get().getCity());
        }

        @Test
        void findById_withExistingId_shouldReturnLocation() {
            Location location = createTestLocation(1L, "New York", "NY", "10001");
            repository.save(location);
            Optional<Location> foundLocation = repository.findById(1L);
            assertTrue(foundLocation.isPresent());
            assertEquals(location, foundLocation.get());
        }

        @Test
        void findById_withNonExistentId_shouldReturnEmpty() {
            Optional<Location> found = repository.findById(999L);
            assertTrue(found.isEmpty());
        }

        @Test
        void findAll_withMultipleLocations_shouldReturnAllLocations() {
            Location loc1 = createTestLocation(1L, "New York", "NY", "10001");
            Location loc2 = createTestLocation(2L, "Chicago", "IL", "60601");
            repository.save(loc1);
            repository.save(loc2);

            Iterable<Location> locationsIterable = repository.findAll();
            List<Location> locations = new ArrayList<>();
            locationsIterable.forEach(locations::add);

            assertEquals(2, locations.size());
            assertTrue(locations.contains(loc1));
            assertTrue(locations.contains(loc2));
        }

        @Test
        void deleteById_withExistingId_shouldRemoveLocation() {
            Location location = createTestLocation(1L, "New York", "NY", "10001");
            repository.save(location);
            repository.deleteById(1L);
            assertFalse(repository.findById(1L).isPresent());
        }
    }

    @Nested
    @DisplayName("Custom Finder and Filter Operations")
    class FinderAndFilterTests {
        @Test
        @DisplayName("Should find locations by city case-insensitively")
        void findByCityIgnoreCase_withMixedCaseCity_shouldReturnMatchingLocations() {
            Location loc1 = createTestLocation(1L, "New York", "NY", "10001");
            Location loc2 = createTestLocation(2L, "new york", "NY", "10002");
            Location loc3 = createTestLocation(3L, "Chicago", "IL", "60601");
            repository.save(loc1);
            repository.save(loc2);
            repository.save(loc3);

            List<Location> newYorkLocations = repository.findByCityIgnoreCase("New York");
            assertEquals(2, newYorkLocations.size());
            assertTrue(newYorkLocations.contains(loc1));
            assertTrue(newYorkLocations.contains(loc2));
        }

        @Test
        @DisplayName("Should find locations by state case-insensitively")
        void findByStateIgnoreCase_withMixedCaseState_shouldReturnMatchingLocations() {
            Location loc1 = createTestLocation(1L, "Miami", "FL", "33101");
            Location loc2 = createTestLocation(2L, "Orlando", "fl", "32801");
            Location loc3 = createTestLocation(3L, "Atlanta", "GA", "30301");
            repository.save(loc1);
            repository.save(loc2);
            repository.save(loc3);

            List<Location> floridaLocations = repository.findByStateIgnoreCase("Fl");
            assertEquals(2, floridaLocations.size());
            assertTrue(floridaLocations.contains(loc1));
            assertTrue(floridaLocations.contains(loc2));
        }

        @Test
        @DisplayName("Should find location by zip code")
        void findByZipCode_withMatchingLocation_shouldReturnLocation() {
            Location loc1 = createTestLocation(1L, "New York", "NY", "10001");
            Location loc2 = createTestLocation(2L, "Chicago", "IL", "60601");
            repository.save(loc1);
            repository.save(loc2);

            List<Location> found = repository.findByZipCode("60601");
            assertEquals(1, found.size());
            assertEquals(loc2, found.get(0));
        }

        @Test
        @DisplayName("Should find locations by filter")
        void findByFilter_withMatchingLocations_shouldReturnMatchingLocations() {
            Location loc1 = createTestLocation(1L, "New York", "NY", "10001");
            Location loc2 = createTestLocation(2L, "New York", "NY", "10002");
            repository.save(loc1);
            repository.save(loc2);

            LocationFilter filter = LocationFilter.ofCity("New York");
            List<Location> filteredLocations = repository.findByFilter(filter);
            assertEquals(2, filteredLocations.size());
        }
    }
}