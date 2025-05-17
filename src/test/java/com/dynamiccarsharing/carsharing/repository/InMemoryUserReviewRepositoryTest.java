package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.filter.UserReviewFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryUserReviewRepositoryTest {

    private InMemoryUserReviewRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserReviewRepository();
        repository.findAll().forEach(review -> repository.deleteById(review.getId()));
    }

    private UserReview createTestUserReview(Long id, String comment) {
        return new UserReview(id, 2L, comment);
    }

    @Test
    void save_shouldSaveAndReturnUserReview() {
        UserReview review = createTestUserReview(1L, "Great driver!");

        UserReview savedReview = repository.save(review);

        assertEquals(review, savedReview);
        assertTrue(repository.findById(1L).isPresent());
        assertEquals(review, repository.findById(1L).get());
    }

    @Test
    void save_withNullUserReview_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
    }

    @Test
    void findById_withExistingId_shouldReturnUserReview() {
        UserReview review = createTestUserReview(1L, "Great driver!");
        repository.save(review);

        Optional<UserReview> foundReview = repository.findById(1L);

        assertTrue(foundReview.isPresent());
        assertEquals(review, foundReview.get());
    }

    @Test
    void findById_withNonExistingId_shouldReturnEmpty() {
        Optional<UserReview> foundReview = repository.findById(1L);

        assertFalse(foundReview.isPresent());
    }

    @Test
    void deleteById_withExistingId_shouldRemoveUserReview() {
        UserReview review = createTestUserReview(1L, "Great driver!");
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
    void findAll_withMultipleUserReviews_shouldReturnAllUserReviews() {
        UserReview review1 = createTestUserReview(1L, "Great driver!");
        UserReview review2 = createTestUserReview(2L, "Needs improvement");
        repository.save(review1);
        repository.save(review2);

        Iterable<UserReview> reviews = repository.findAll();
        List<UserReview> reviewList = new ArrayList<>();
        reviews.forEach(reviewList::add);

        assertEquals(2, reviewList.size());
        assertTrue(reviewList.contains(review1));
        assertTrue(reviewList.contains(review2));
    }

    @Test
    void findAll_withSingleUserReview_shouldReturnSingleUserReview() {
        UserReview review = createTestUserReview(1L, "Great driver!");
        repository.save(review);

        Iterable<UserReview> reviews = repository.findAll();
        List<UserReview> reviewList = new ArrayList<>();
        reviews.forEach(reviewList::add);

        assertEquals(1, reviewList.size());
        assertEquals(review, reviewList.get(0));
    }

    @Test
    void findAll_withNoUserReviews_shouldReturnEmptyIterable() {
        Iterable<UserReview> reviews = repository.findAll();
        List<UserReview> reviewList = new ArrayList<>();
        reviews.forEach(reviewList::add);

        assertEquals(0, reviewList.size());
    }

    @Test
    void findByFilter_withMatchingUserReviews_shouldReturnMatchingUserReviews() {
        UserReview review1 = createTestUserReview(1L, "Great driver!");
        UserReview review2 = createTestUserReview(2L, "Needs improvement");
        UserReview review3 = createTestUserReview(3L, "Awesome!");
        repository.save(review1);
        repository.save(review2);
        repository.save(review3);
        UserReviewFilter filter = mock(UserReviewFilter.class);
        when(filter.test(any(UserReview.class))).thenAnswer(invocation -> {
            UserReview review = invocation.getArgument(0);
            return review.getComment().contains("!");
        });

        List<UserReview> filteredReviews = repository.findByFilter(filter);

        assertEquals(2, filteredReviews.size());
        assertTrue(filteredReviews.contains(review1));
        assertTrue(filteredReviews.contains(review3));
        assertFalse(filteredReviews.contains(review2));
    }

    @Test
    void findByFilter_withNoMatchingUserReviews_shouldReturnEmptyList() {
        UserReview review = createTestUserReview(1L, "Great driver!");
        repository.save(review);
        UserReviewFilter filter = mock(UserReviewFilter.class);
        when(filter.test(any(UserReview.class))).thenReturn(false);

        List<UserReview> filteredReviews = repository.findByFilter(filter);

        assertEquals(0, filteredReviews.size());
    }
}