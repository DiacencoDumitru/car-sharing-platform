package com.dynamiccarsharing.carsharing.repository.inmemory;

import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.filter.UserReviewFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserReviewRepositoryJdbcImplTest {

    private InMemoryUserReviewRepositoryJdbcImpl repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserReviewRepositoryJdbcImpl();
    }

    private UserReview createTestUserReview(Long id, String comment, User user, User reviewer) {
        return UserReview.builder()
                .id(id)
                .user(user)
                .reviewer(reviewer)
                .comment(comment)
                .build();
    }

    private User createStubUser(Long id) {
        return User.builder().id(id).build();
    }

    @Nested
    @DisplayName("CRUD and FindAll Operations")
    class CrudTests {
        @Test
        void save_shouldSaveAndReturnUserReview() {
            User user = createStubUser(1L);
            User reviewer = createStubUser(2L);
            UserReview review = createTestUserReview(1L, "Great driver!", user, reviewer);
            UserReview savedReview = repository.save(review);
            assertEquals(review, savedReview);
            assertTrue(repository.findById(1L).isPresent());
        }

        @Test
        void save_updateExistingReview_shouldChangeComment() {
            User user = createStubUser(1L);
            User reviewer = createStubUser(2L);
            UserReview original = createTestUserReview(1L, "Good driver", user, reviewer);
            repository.save(original);

            UserReview updated = original.withComment("Excellent driver!");
            repository.save(updated);

            Optional<UserReview> found = repository.findById(1L);
            assertTrue(found.isPresent());
            assertEquals("Excellent driver!", found.get().getComment());
        }

        @Test
        void findById_withExistingId_shouldReturnUserReview() {
            User user = createStubUser(1L);
            User reviewer = createStubUser(2L);
            UserReview review = createTestUserReview(1L, "Great driver!", user, reviewer);
            repository.save(review);
            Optional<UserReview> foundReview = repository.findById(1L);
            assertTrue(foundReview.isPresent());
            assertEquals(review, foundReview.get());
        }

        @Test
        void findAll_withMultipleUserReviews_shouldReturnAllReviews() {
            User user = createStubUser(1L);
            User reviewer = createStubUser(2L);
            UserReview review1 = createTestUserReview(1L, "Review 1", user, reviewer);
            UserReview review2 = createTestUserReview(2L, "Review 2", reviewer, user);
            repository.save(review1);
            repository.save(review2);

            Iterable<UserReview> reviewsIterable = repository.findAll();
            List<UserReview> reviews = new ArrayList<>();
            reviewsIterable.forEach(reviews::add);

            assertEquals(2, reviews.size());
        }

        @Test
        void deleteById_withExistingId_shouldRemoveReview() {
            User user = createStubUser(1L);
            User reviewer = createStubUser(2L);
            UserReview review = createTestUserReview(1L, "To delete", user, reviewer);
            repository.save(review);
            repository.deleteById(1L);
            assertFalse(repository.findById(1L).isPresent());
        }
    }

    @Nested
    @DisplayName("Custom Finder and Filter Operations")
    class FinderAndFilterTests {
        @Test
        @DisplayName("Should find reviews by user ID")
        void findByUserId_withMatchingReviews_shouldReturnMatchingReviews() {
            User user1 = createStubUser(1L);
            User user2 = createStubUser(2L);
            User reviewer = createStubUser(3L);
            UserReview review1 = createTestUserReview(1L, "Review for user 1", user1, reviewer);
            UserReview review2 = createTestUserReview(2L, "Review for user 2", user2, reviewer);
            repository.save(review1);
            repository.save(review2);

            List<UserReview> user1Reviews = repository.findByUserId(1L);
            assertEquals(1, user1Reviews.size());
            assertEquals(review1, user1Reviews.get(0));
        }

        @Test
        @DisplayName("Should find reviews by reviewer ID")
        void findByReviewerId_withMatchingReviews_shouldReturnMatchingReviews() {
            User user = createStubUser(1L);
            User reviewer1 = createStubUser(2L);
            User reviewer2 = createStubUser(3L);
            UserReview review1 = createTestUserReview(1L, "Review from reviewer 1", user, reviewer1);
            UserReview review2 = createTestUserReview(2L, "Review from reviewer 2", user, reviewer2);
            repository.save(review1);
            repository.save(review2);

            List<UserReview> reviewer1Reviews = repository.findByReviewerId(2L);
            assertEquals(1, reviewer1Reviews.size());
            assertEquals(review1, reviewer1Reviews.get(0));
        }

        @Test
        @DisplayName("Should find reviews by filter")
        void findByFilter_withMatchingUserReviews_shouldReturnMatchingUserReviews() {
            User user1 = createStubUser(1L);
            User user2 = createStubUser(2L);
            UserReview review1 = createTestUserReview(1L, "Review about user 1", user1, user2);
            UserReview review2 = createTestUserReview(2L, "Review about user 2", user2, user1);
            repository.save(review1);
            repository.save(review2);

            UserReviewFilter filter = UserReviewFilter.ofUserId(1L);
            List<UserReview> filteredReviews = repository.findByFilter(filter);
            assertEquals(1, filteredReviews.size());
            assertEquals(review1, filteredReviews.get(0));
        }
    }
}