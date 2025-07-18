package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.repository.filter.CarReviewFilter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CarReviewDaoTest extends BaseDaoTest {
    @Autowired
    private CarReviewDao carReviewDao;

    private Long carId1;
    private Long carId2;
    private Long reviewerId1;
    private Long reviewerId2;

    @BeforeEach
    void setUp() throws SQLException {
        createTestDependencies();
    }

    private void createTestDependencies() throws SQLException {
        Long contactInfoId1 = createContactInfo("reviewer1@example.com", "+111", "Reviewer", "One");
        this.reviewerId1 = createUser(contactInfoId1, "RENTER", "ACTIVE");
        Long contactInfoId2 = createContactInfo("reviewer2@example.com", "+222", "Reviewer", "Two");
        this.reviewerId2 = createUser(contactInfoId2, "RENTER", "ACTIVE");

        Long locationId = createLocation("Test City", "TS", "12345");
        this.carId1 = createCar("CAR1", "Toyota", "Camry", locationId);
        this.carId2 = createCar("CAR2", "Honda", "Civic", locationId);
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save a new review successfully")
        void save_newReview_shouldSaveSuccessfully() {
            CarReview review = new CarReview(null, reviewerId1, carId1, "Great car!");
            CarReview saved = carReviewDao.save(review);

            assertNotNull(saved.getId());
            assertEquals("Great car!", saved.getComment());
        }

        @Test
        @DisplayName("Should update an existing review")
        void save_existingReview_shouldUpdate() {
            CarReview original = carReviewDao.save(new CarReview(null, reviewerId1, carId1, "Good car."));
            CarReview updated = original.withComment("Actually, it was a great car!");
            CarReview result = carReviewDao.save(updated);

            assertEquals(original.getId(), result.getId());
            assertEquals("Actually, it was a great car!", result.getComment());
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        @Test
        @DisplayName("Should find review by valid ID")
        void findById_validId_shouldReturnReview() {
            CarReview saved = carReviewDao.save(new CarReview(null, reviewerId1, carId1, "A review."));
            Optional<CarReview> found = carReviewDao.findById(saved.getId());

            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
        }

        @Test
        @DisplayName("Should return empty for non-existent ID")
        void findById_nonExistentId_shouldReturnEmpty() {
            Optional<CarReview> found = carReviewDao.findById(999L);
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should find all reviews")
        void findAll_withData_shouldReturnAll() {
            carReviewDao.save(new CarReview(null, reviewerId1, carId1, "Review 1"));
            carReviewDao.save(new CarReview(null, reviewerId2, carId2, "Review 2"));
            List<CarReview> reviews = (List<CarReview>) carReviewDao.findAll();
            assertEquals(2, reviews.size());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {
        @Test
        @DisplayName("Should delete review by ID")
        void deleteById_validId_shouldDelete() {
            CarReview saved = carReviewDao.save(new CarReview(null, reviewerId1, carId1, "To be deleted."));
            carReviewDao.deleteById(saved.getId());
            Optional<CarReview> found = carReviewDao.findById(saved.getId());
            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperations {
        @Test
        @DisplayName("Should find reviews by car ID")
        void findByFilter_byCarId_shouldReturnMatching() throws SQLException {
            carReviewDao.save(new CarReview(null, reviewerId1, carId1, "Review for car 1"));
            carReviewDao.save(new CarReview(null, reviewerId2, carId2, "Review for car 2"));

            CarReviewFilter filter = CarReviewFilter.ofCarId(carId1);
            List<CarReview> results = carReviewDao.findByFilter(filter);

            assertEquals(1, results.size());
            assertEquals(carId1, results.get(0).getCarId());
        }

        @Test
        @DisplayName("Should return empty list for non-matching filter")
        void findByFilter_noMatches_shouldReturnEmpty() throws SQLException {
            carReviewDao.save(new CarReview(null, reviewerId1, carId2, "A review."));
            CarReviewFilter filter = CarReviewFilter.ofCarId(carId1);
            List<CarReview> results = carReviewDao.findByFilter(filter);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Should return all reviews for null filter")
        void findByFilter_nullFilter_shouldReturnAll() throws SQLException {
            carReviewDao.save(new CarReview(null, reviewerId1, carId1, "Review 1"));
            carReviewDao.save(new CarReview(null, reviewerId2, carId2, "Review 2"));

            List<CarReview> results = carReviewDao.findByFilter(null);
            assertEquals(2, results.size());
        }
    }
}