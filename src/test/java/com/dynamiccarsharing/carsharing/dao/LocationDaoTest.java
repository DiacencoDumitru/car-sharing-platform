package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.filter.LocationFilter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("jdbc")
class LocationDaoTest extends BaseDaoTest {
    @Autowired
    private LocationDao locationDao;

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save new location successfully")
        void save_newLocation_shouldSaveSuccessfully() {
            Location loc = Location.builder()
                    .city("New York")
                    .state("NY")
                    .zipCode("10001")
                    .build();
            Location saved = locationDao.save(loc);
            assertNotNull(saved.getId());
            assertEquals("New York", saved.getCity());
        }

        @Test
        @DisplayName("Should update existing location")
        void save_existingLocation_shouldUpdate() throws SQLException {
            Location original = createLocation("Los Angeles", "CA", "90001");
            Location updated = original.withCity("LA");

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
        void findById_validId_shouldReturnLocation() throws SQLException {
            Location saved = createLocation("Chicago", "IL", "60601");

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
    @DisplayName("Delete Operations")
    class DeleteOperations {
        @Test
        @DisplayName("Should delete location by ID")
        void deleteById_validId_shouldDelete() throws SQLException {
            Location loc = createLocation("To Be Deleted", "DEL", "00000");
            locationDao.deleteById(loc.getId());
            Optional<Location> found = locationDao.findById(loc.getId());
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should not throw exception when deleting non-existent location")
        void deleteById_nonExistentId_shouldNotThrow() {
            assertDoesNotThrow(() -> locationDao.deleteById(999L));
        }
    }

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperations {
        @BeforeEach
        void setUpData() throws SQLException {
            createLocation("Miami", "FL", "33101");
            createLocation("Miami", "FL", "33102");
            createLocation("Houston", "TX", "77001");
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
        @DisplayName("Should return all for empty filter")
        void findByFilter_emptyFilter_shouldReturnAll() throws SQLException {
            LocationFilter filter = LocationFilter.of(null, null, null);
            List<Location> results = locationDao.findByFilter(filter);
            assertEquals(3, results.size());
        }
    }

    @Nested
    @DisplayName("Edge Case and Specific Finders")
    class EdgeCases {
        @Test
        @DisplayName("Should find by state case-insensitively")
        void findByStateIgnoreCase_shouldReturnMatching() throws SQLException {
            createLocation("Jacksonville", "FL", "32201");
            createLocation("Orlando", "fl", "32801");
            createLocation("Tampa", "Fl", "33601");
            createLocation("Atlanta", "GA", "30301");

            List<Location> floridaLocations = locationDao.findByStateIgnoreCase("fl");
            assertEquals(3, floridaLocations.size());
        }

        @Test
        @DisplayName("Should find by zip code")
        void findByZipCode_shouldReturnCorrectLocation() throws SQLException {
            createLocation("Unique City", "UC", "99999");
            Optional<Location> found = locationDao.findByZipCode("99999");
            assertTrue(found.isPresent());
            assertEquals("UC", found.get().getState());
        }
    }
}