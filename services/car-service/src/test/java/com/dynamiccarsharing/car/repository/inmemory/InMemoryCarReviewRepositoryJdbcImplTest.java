package com.dynamiccarsharing.car.repository.inmemory;

import com.dynamiccarsharing.car.filter.CarReviewFilter;
import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.model.CarReview;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryCarReviewRepositoryJdbcImplTest {

    private InMemoryCarReviewRepositoryJdbcImpl repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCarReviewRepositoryJdbcImpl();
    }

    private CarReview createTestCarReview(Long id, String comment, Long reviewerId, Car car) {
        return CarReview.builder()
                .id(id)
                .reviewerId(reviewerId)
                .car(car)
                .comment(comment)
                .build();
    }

    private Car createStubCar(Long id) {
        return Car.builder().id(id).build();
    }

    @Nested
    @DisplayName("CRUD and Filter Operations")
    class CrudAndFilterTests {
        @Test
        void save_shouldSaveAndReturnCarReview() {
            Long reviewerId = 1L;
            Car car = createStubCar(1L);
            CarReview review = createTestCarReview(1L, "Great car!", reviewerId, car);
            CarReview savedReview = repository.save(review);
            assertEquals(review, savedReview);
            assertTrue(repository.findById(1L).isPresent());
        }

        @Test
        void save_updateExistingReview_shouldChangeComment() {
            Long reviewerId = 1L;
            Car car = createStubCar(1L);
            CarReview original = createTestCarReview(1L, "Good car", reviewerId, car);
            repository.save(original);

            CarReview updated = original.toBuilder().comment("Excellent car!").build();
            repository.save(updated);

            Optional<CarReview> found = repository.findById(1L);
            assertTrue(found.isPresent());
            assertEquals("Excellent car!", found.get().getComment());
        }

        @Test
        void findById_withExistingId_shouldReturnCarReview() {
            Long reviewerId = 1L;
            Car car = createStubCar(1L);
            CarReview review = createTestCarReview(1L, "Great car!", reviewerId, car);
            repository.save(review);
            Optional<CarReview> foundReview = repository.findById(1L);
            assertTrue(foundReview.isPresent());
            assertEquals(review, foundReview.get());
        }

        @Test
        void deleteById_withExistingId_shouldRemoveReview() {
            Long reviewerId = 1L;
            Car car = createStubCar(1L);
            CarReview review = createTestCarReview(1L, "To be deleted", reviewerId, car);
            repository.save(review);

            repository.deleteById(1L);
            assertFalse(repository.findById(1L).isPresent());
        }

        @Test
        void findAll_withMultipleCarReviews_shouldReturnAllReviews() {
            Long reviewerId = 1L;
            Car car = createStubCar(1L);
            CarReview review1 = createTestCarReview(1L, "Review 1", reviewerId, car);
            CarReview review2 = createTestCarReview(2L, "Review 2", reviewerId, car);
            repository.save(review1);
            repository.save(review2);

            Iterable<CarReview> reviewsIterable = repository.findAll();
            List<CarReview> reviews = new ArrayList<>();
            reviewsIterable.forEach(reviews::add);

            assertEquals(2, reviews.size());
        }

        @Test
        void findByFilter_withMatchingCarReviews_shouldReturnMatchingCarReviews() {
            Long reviewerId1 = 1L;
            Long reviewerId2 = 2L;
            Car car = createStubCar(1L);
            CarReview review1 = createTestCarReview(1L, "Great car!", reviewerId1, car);
            CarReview review2 = createTestCarReview(2L, "Needs maintenance", reviewerId2, car);
            CarReview review3 = createTestCarReview(3L, "Awesome ride!", reviewerId1, car);
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