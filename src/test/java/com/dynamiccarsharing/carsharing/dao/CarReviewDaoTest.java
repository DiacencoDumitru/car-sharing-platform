package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.filter.CarReviewFilter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("jdbc")
class CarReviewDaoTest extends BaseDaoTest {
    @Autowired
    private CarReviewDao carReviewDao;

    private Car car1;
    private Car car2;
    private User reviewer1;
    private User reviewer2;

    @BeforeEach
    void setUp() throws SQLException {
        createTestDependencies();
    }

    private void createTestDependencies() throws SQLException {
        Location location = createLocation("Test City", "TS", "12345");

        ContactInfo contactInfo1 = createContactInfo("reviewer1@example.com", "+111", "Reviewer", "One");
        this.reviewer1 = createUser(contactInfo1, UserRole.RENTER, UserStatus.ACTIVE);

        ContactInfo contactInfo2 = createContactInfo("reviewer2@example.com", "+222", "Reviewer", "Two");
        this.reviewer2 = createUser(contactInfo2, UserRole.RENTER, UserStatus.ACTIVE);

        this.car1 = createCar("CAR1", "Toyota", "Camry", location);
        this.car2 = createCar("CAR2", "Honda", "Civic", location);
    }

    private CarReview createUnsavedReview(User reviewer, Car car, String comment) {
        return CarReview.builder()
                .reviewer(reviewer)
                .car(car)
                .comment(comment)
                .build();
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save a new review successfully")
        void save_newReview_shouldSaveSuccessfully() {
            CarReview review = createUnsavedReview(reviewer1, car1, "Great car!");
            CarReview saved = carReviewDao.save(review);
            assertNotNull(saved.getId());
            assertEquals("Great car!", saved.getComment());
        }

        @Test
        @DisplayName("Should update an existing review")
        void save_existingReview_shouldUpdate() {
            CarReview original = carReviewDao.save(createUnsavedReview(reviewer1, car1, "Good car."));
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
            CarReview saved = carReviewDao.save(createUnsavedReview(reviewer1, car1, "A review."));
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
            carReviewDao.save(createUnsavedReview(reviewer1, car1, "Review 1"));
            carReviewDao.save(createUnsavedReview(reviewer2, car2, "Review 2"));
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
            CarReview saved = carReviewDao.save(createUnsavedReview(reviewer1, car1, "To be deleted."));
            carReviewDao.deleteById(saved.getId());
            Optional<CarReview> found = carReviewDao.findById(saved.getId());
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("Should not throw exception for non-existent ID")
        void deleteById_nonExistentId_shouldNotThrow() {
            assertDoesNotThrow(() -> carReviewDao.deleteById(999L));
        }
    }

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperations {
        @BeforeEach
        void setUpData() {
            carReviewDao.save(createUnsavedReview(reviewer1, car1, "Review from user 1 on car 1."));
            carReviewDao.save(createUnsavedReview(reviewer1, car2, "Review from user 1 on car 2."));
            carReviewDao.save(createUnsavedReview(reviewer2, car1, "Review from user 2 on car 1."));
        }

        @Test
        @DisplayName("Should find reviews by reviewer ID")
        void findByFilter_byReviewerId_shouldReturnMatching() throws SQLException {
            CarReviewFilter filter = CarReviewFilter.ofReviewerId(reviewer1.getId());
            List<CarReview> results = carReviewDao.findByFilter(filter);
            assertEquals(2, results.size());
            assertTrue(results.stream().allMatch(r -> r.getReviewer().getId().equals(reviewer1.getId())));
        }

        @Test
        @DisplayName("Should find reviews by car ID")
        void findByFilter_byCarId_shouldReturnMatching() throws SQLException {
            CarReviewFilter filter = CarReviewFilter.ofCarId(car1.getId());
            List<CarReview> results = carReviewDao.findByFilter(filter);
            assertEquals(2, results.size());
            assertTrue(results.stream().allMatch(r -> r.getCar().getId().equals(car1.getId())));
        }

        @Test
        @DisplayName("Should return empty list for non-matching filter")
        void findByFilter_noMatches_shouldReturnEmpty() throws SQLException {
            CarReviewFilter filter = CarReviewFilter.ofReviewerId(999L);
            List<CarReview> results = carReviewDao.findByFilter(filter);
            assertTrue(results.isEmpty());
        }
    }
}