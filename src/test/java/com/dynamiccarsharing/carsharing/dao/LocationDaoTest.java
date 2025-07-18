package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.filter.LocationFilter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LocationDaoTest extends BaseDaoTest {
    @Autowired
    private LocationDao locationDao;

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save new location successfully")
        void save_newLocation_shouldSaveSuccessfully() {
            Location loc = new Location(null, "New York", "NY", "10001");
            Location saved = locationDao.save(loc);
            assertNotNull(saved.getId());
            assertEquals("New York", saved.getCity());
        }

        @Test
        @DisplayName("Should update existing location")
        void save_existingLocation_shouldUpdate() {
            Location original = locationDao.save(new Location(null, "Los Angeles", "CA", "90001"));
            Location updated = new Location(original.getId(), "LA", "CA", "90001");
            Location result = locationDao.save(updated);
            assertEquals(original.getId(), result.getId());
            assertEquals("LA", result.getCity());
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        @Test
        @DisplayName("Should find location by valid ID")
        void findById_validId_shouldReturnLocation() {
            Location saved = locationDao.save(new Location(null, "Chicago", "IL", "60601"));
            Optional<Location> found = locationDao.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
        }

        @Test
        @DisplayName("Should return empty for non-existent ID")
        void findById_nonExistentId_shouldReturnEmpty() {
            Optional<Location> found = locationDao.findById(999L);
            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperations {
        @BeforeEach
        void setUpData() {
            locationDao.save(new Location(null, "Miami", "FL", "33101"));
            locationDao.save(new Location(null, "Miami", "FL", "33102"));
            locationDao.save(new Location(null, "Houston", "TX", "77001"));
        }

        @Test
        @DisplayName("Should find by city filter")
        void findByFilter_byCity_shouldReturnMatching() throws SQLException {
            LocationFilter filter = LocationFilter.ofCity("Miami");
            List<Location> results = locationDao.findByFilter(filter);
            assertEquals(2, results.size());
        }

        @Test
        @DisplayName("Should return empty list for non-matching filter")
        void findByFilter_noMatches_shouldReturnEmpty() throws SQLException {
            LocationFilter filter = LocationFilter.ofCity("Boston");
            List<Location> results = locationDao.findByFilter(filter);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Should return all for null filter")
        void findByFilter_nullFilter_shouldReturnAll() throws SQLException {
            List<Location> results = locationDao.findByFilter(null);
            assertEquals(3, results.size());
        }
    }
}