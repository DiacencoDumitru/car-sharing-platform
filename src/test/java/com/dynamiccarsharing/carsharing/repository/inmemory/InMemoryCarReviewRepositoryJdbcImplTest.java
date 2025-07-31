package com.dynamiccarsharing.carsharing.repository.inmemory;

import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.filter.CarReviewFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCarReviewRepositoryJdbcImplTest {

    private InMemoryCarReviewRepositoryJdbcImpl repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCarReviewRepositoryJdbcImpl();
    }

    private CarReview createTestCarReview(Long id, String comment, User reviewer, Car car) {
        return CarReview.builder()
                .id(id)
                .reviewer(reviewer)
                .car(car)
                .comment(comment)
                .build();
    }

    private User createStubUser(Long id) {
        return User.builder().id(id).build();
    }

    private Car createStubCar(Long id) {
        return Car.builder().id(id).build();
    }

    @Nested
    @DisplayName("CRUD and Filter Operations")
    class CrudAndFilterTests {
        @Test
        void save_shouldSaveAndReturnCarReview() {
            User reviewer = createStubUser(1L);
            Car car = createStubCar(1L);
            CarReview review = createTestCarReview(1L, "Great car!", reviewer, car);
            CarReview savedReview = repository.save(review);
            assertEquals(review, savedReview);
            assertTrue(repository.findById(1L).isPresent());
        }

        @Test
        void save_updateExistingReview_shouldChangeComment() {
            User reviewer = createStubUser(1L);
            Car car = createStubCar(1L);
            CarReview original = createTestCarReview(1L, "Good car", reviewer, car);
            repository.save(original);

            CarReview updated = original.withComment("Excellent car!");
            repository.save(updated);

            Optional<CarReview> found = repository.findById(1L);
            assertTrue(found.isPresent());
            assertEquals("Excellent car!", found.get().getComment());
        }

        @Test
        void findById_withExistingId_shouldReturnCarReview() {
            User reviewer = createStubUser(1L);
            Car car = createStubCar(1L);
            CarReview review = createTestCarReview(1L, "Great car!", reviewer, car);
            repository.save(review);
            Optional<CarReview> foundReview = repository.findById(1L);
            assertTrue(foundReview.isPresent());
            assertEquals(review, foundReview.get());
        }

        @Test
        void deleteById_withExistingId_shouldRemoveReview() {
            User reviewer = createStubUser(1L);
            Car car = createStubCar(1L);
            CarReview review = createTestCarReview(1L, "To be deleted", reviewer, car);
            repository.save(review);

            repository.deleteById(1L);
            assertFalse(repository.findById(1L).isPresent());
        }

        @Test
        void findAll_withMultipleCarReviews_shouldReturnAllReviews() {
            User reviewer = createStubUser(1L);
            Car car = createStubCar(1L);
            CarReview review1 = createTestCarReview(1L, "Review 1", reviewer, car);
            CarReview review2 = createTestCarReview(2L, "Review 2", reviewer, car);
            repository.save(review1);
            repository.save(review2);

            Iterable<CarReview> reviewsIterable = repository.findAll();
            List<CarReview> reviews = new ArrayList<>();
            reviewsIterable.forEach(reviews::add);

            assertEquals(2, reviews.size());
        }

        @Test
        void findByFilter_withMatchingCarReviews_shouldReturnMatchingCarReviews() {
            User reviewer1 = createStubUser(1L);
            User reviewer2 = createStubUser(2L);
            Car car = createStubCar(1L);
            CarReview review1 = createTestCarReview(1L, "Great car!", reviewer1, car);
            CarReview review2 = createTestCarReview(2L, "Needs maintenance", reviewer2, car);
            CarReview review3 = createTestCarReview(3L, "Awesome ride!", reviewer1, car);
            repository.save(review1);
            repository.save(review2);
            repository.save(review3);

            CarReviewFilter filter = CarReviewFilter.ofReviewerId(1L);
            List<CarReview> filteredReviews = repository.findByFilter(filter);

            assertEquals(2, filteredReviews.size());
            assertTrue(filteredReviews.contains(review1));
            assertTrue(filteredReviews.contains(review3));
        }
    }
}