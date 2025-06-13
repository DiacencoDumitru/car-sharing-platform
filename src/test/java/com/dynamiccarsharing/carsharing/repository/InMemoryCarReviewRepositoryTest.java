package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.repository.filter.CarReviewFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryCarReviewRepositoryTest {

    private InMemoryCarReviewRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCarReviewRepository();
        repository.findAll().forEach(review -> repository.deleteById(review.getId()));
    }

    private CarReview createTestCarReview(Long id, String comment) {
        return new CarReview(id, 2L, 2L, comment);
    }

    @Test
    void save_shouldSaveAndReturnCarReview() {
        CarReview review = createTestCarReview(1L, "Great car!");

        CarReview savedReview = repository.save(review);

        assertEquals(review, savedReview);
        assertTrue(repository.findById(1L).isPresent());
        assertEquals(review, repository.findById(1L).get());
    }

    @Test
    void save_withNullCarReview_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
    }

    @Test
    void findById_withExistingId_shouldReturnCarReview() {
        CarReview review = createTestCarReview(1L, "Great car!");
        repository.save(review);

        Optional<CarReview> foundReview = repository.findById(1L);

        assertTrue(foundReview.isPresent());
        assertEquals(review, foundReview.get());
    }

    @Test
    void findById_withNonExistingId_shouldReturnEmpty() {
        Optional<CarReview> foundReview = repository.findById(1L);

        assertFalse(foundReview.isPresent());
    }

    @Test
    void deleteById_withExistingId_shouldRemoveCarReview() {
        CarReview review = createTestCarReview(1L, "Great car!");
        repository.save(review);

        repository.deleteById(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void deleteById_withNonExistingId_shouldDoNothing() {
        repository.deleteById(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void findAll_withMultipleCarReviews_shouldReturnAllCarReviews() {
        CarReview review1 = createTestCarReview(1L, "Great car!");
        CarReview review2 = createTestCarReview(2L, "Needs maintenance");
        repository.save(review1);
        repository.save(review2);

        Iterable<CarReview> reviews = repository.findAll();
        List<CarReview> reviewList = new ArrayList<>();
        reviews.forEach(reviewList::add);

        assertEquals(2, reviewList.size());
        assertTrue(reviewList.contains(review1));
        assertTrue(reviewList.contains(review2));
    }

    @Test
    void findAll_withSingleCarReview_shouldReturnSingleCarReview() {
        CarReview review = createTestCarReview(1L, "Great car!");
        repository.save(review);

        Iterable<CarReview> reviews = repository.findAll();
        List<CarReview> reviewList = new ArrayList<>();
        reviews.forEach(reviewList::add);

        assertEquals(1, reviewList.size());
        assertEquals(review, reviewList.get(0));
    }

    @Test
    void findAll_withNoCarReviews_shouldReturnEmptyIterable() {
        Iterable<CarReview> reviews = repository.findAll();
        List<CarReview> reviewList = new ArrayList<>();
        reviews.forEach(reviewList::add);

        assertEquals(0, reviewList.size());
    }

    @Test
    void findByFilter_withMatchingCarReviews_shouldReturnMatchingCarReviews() {
        CarReview review1 = createTestCarReview(1L, "Great car!");
        CarReview review2 = createTestCarReview(2L, "Needs maintenance");
        CarReview review3 = createTestCarReview(3L, "Awesome ride!");
        repository.save(review1);
        repository.save(review2);
        repository.save(review3);
        CarReviewFilter filter = mock(CarReviewFilter.class);
        when(filter.test(any(CarReview.class))).thenAnswer(invocation -> {
            CarReview review = invocation.getArgument(0);
            return review.getComment().contains("!");
        });

        List<CarReview> filteredReviews = repository.findByFilter(filter);

        assertEquals(2, filteredReviews.size());
        assertTrue(filteredReviews.contains(review1));
        assertTrue(filteredReviews.contains(review3));
        assertFalse(filteredReviews.contains(review2));
    }

    @Test
    void findByFilter_withNoMatchingCarReviews_shouldReturnEmptyList() {
        CarReview review = createTestCarReview(1L, "Great car!");
        repository.save(review);
        CarReviewFilter filter = mock(CarReviewFilter.class);
        when(filter.test(any(CarReview.class))).thenReturn(false);

        List<CarReview> filteredReviews = repository.findByFilter(filter);

        assertEquals(0, filteredReviews.size());
    }
}