package com.dynamiccarsharing.carsharing.dao;

import com.dynamiccarsharing.carsharing.model.UserReview;
import com.dynamiccarsharing.carsharing.repository.filter.UserReviewFilter;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserReviewDaoTest extends BaseDaoTest {
    private UserReviewDao userReviewDao;
    private Long reviewedUserId;
    private Long reviewerId;

    @BeforeEach
    void setUp() throws SQLException {
        userReviewDao = new UserReviewDao(databaseUtil);
        createTestDependencies();
    }

    private void createTestDependencies() throws SQLException {
        Long contactInfo1 = createContactInfo("reviewed@example.com", "+111", "Reviewed", "User");
        this.reviewedUserId = createUser(contactInfo1, "RENTER", "ACTIVE");
        Long contactInfo2 = createContactInfo("reviewer@example.com", "+222", "Reviewer", "User");
        this.reviewerId = createUser(contactInfo2, "RENTER", "ACTIVE");
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save a new review successfully")
        void save_newReview_shouldSave() {
            UserReview review = new UserReview(null, reviewedUserId, reviewerId, "Excellent renter.");
            UserReview saved = userReviewDao.save(review);

            assertNotNull(saved.getId());
            assertEquals("Excellent renter.", saved.getComment());
            assertEquals(reviewedUserId, saved.getUserId());
        }

        @Test
        @DisplayName("Should update an existing review")
        void save_existingReview_shouldUpdate() {
            UserReview original = userReviewDao.save(new UserReview(null, reviewedUserId, reviewerId, "Good."));
            UserReview updated = original.withComment("Very good.");
            UserReview result = userReviewDao.save(updated);

            assertEquals(original.getId(), result.getId());
            assertEquals("Very good.", result.getComment());
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {
        @Test
        @DisplayName("Should find review by valid ID")
        void findById_validId_shouldReturnReview() {
            UserReview saved = userReviewDao.save(new UserReview(null, reviewedUserId, reviewerId, "A review."));
            Optional<UserReview> found = userReviewDao.findById(saved.getId());

            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
        }

        @Test
        @DisplayName("Should find all reviews")
        void findAll_withData_shouldReturnAll() {
            userReviewDao.save(new UserReview(null, reviewedUserId, reviewerId, "Review 1"));
            userReviewDao.save(new UserReview(null, reviewerId, reviewedUserId, "Review 2"));
            List<UserReview> reviews = (List<UserReview>) userReviewDao.findAll();
            assertEquals(2, reviews.size());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {
        @Test
        @DisplayName("Should delete review by ID")
        void deleteById_validId_shouldDelete() {
            UserReview saved = userReviewDao.save(new UserReview(null, reviewedUserId, reviewerId, "To delete."));
            userReviewDao.deleteById(saved.getId());
            Optional<UserReview> found = userReviewDao.findById(saved.getId());
            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Filter Operations")
    class FilterOperations {
        @Test
        @DisplayName("Should find reviews by user ID")
        void findByFilter_byUserId_shouldReturnMatching() throws SQLException {
            userReviewDao.save(new UserReview(null, reviewedUserId, reviewerId, "Review for user 1"));
            userReviewDao.save(new UserReview(null, reviewerId, reviewedUserId, "Review for user 2"));

            UserReviewFilter filter = UserReviewFilter.ofUserId(reviewedUserId);
            List<UserReview> results = userReviewDao.findByFilter(filter);

            assertEquals(1, results.size());
            assertEquals(reviewedUserId, results.get(0).getUserId());
        }

        @Test
        @DisplayName("Should find reviews by reviewer ID")
        void findByFilter_byReviewerId_shouldReturnMatching() throws SQLException {
            userReviewDao.save(new UserReview(null, reviewedUserId, reviewerId, "Review by reviewer"));
            userReviewDao.save(new UserReview(null, reviewerId, reviewedUserId, "Another review"));

            UserReviewFilter filter = UserReviewFilter.ofReviewerId(reviewerId);
            List<UserReview> results = userReviewDao.findByFilter(filter);

            assertEquals(1, results.size());
            assertEquals(reviewerId, results.get(0).getReviewerId());
        }
    }
}