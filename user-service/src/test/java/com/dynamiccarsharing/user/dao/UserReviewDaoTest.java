package com.dynamiccarsharing.user.dao;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.filter.UserReviewFilter;
import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.user.model.UserReview;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("jdbc")
class UserReviewDaoTest extends UserBaseDaoTest {
    @Autowired
    private UserReviewDao userReviewDao;

    private User reviewedUser;
    private User reviewer;

    @BeforeEach
    void setUp() throws SQLException {
        createTestDependencies();
    }

    private void createTestDependencies() throws SQLException {
        ContactInfo contactInfo1 = createContactInfo("reviewed@example.com", "+111", "Reviewed", "User");
        this.reviewedUser = createUser(contactInfo1, UserRole.RENTER, UserStatus.ACTIVE);
        ContactInfo contactInfo2 = createContactInfo("reviewer@example.com", "+222", "Reviewer", "User");
        this.reviewer = createUser(contactInfo2, UserRole.RENTER, UserStatus.ACTIVE);
    }

    private UserReview createUnsavedReview(User user, User reviewer, String comment) {
        return UserReview.builder()
                .user(user)
                .reviewer(reviewer)
                .comment(comment)
                .build();
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {
        @Test
        @DisplayName("Should save a new review successfully")
        void save_newReview_shouldSave() {
            UserReview review = createUnsavedReview(reviewedUser, reviewer, "Excellent renter.");
            UserReview saved = userReviewDao.save(review);

            assertNotNull(saved.getId());
            assertEquals("Excellent renter.", saved.getComment());
            assertEquals(reviewedUser.getId(), saved.getUser().getId());
        }

        @Test
        @DisplayName("Should update an existing review")
        void save_existingReview_shouldUpdate() {
            UserReview original = userReviewDao.save(createUnsavedReview(reviewedUser, reviewer, "Good."));
            original.setComment("Very good.");
            UserReview result = userReviewDao.save(original);

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
            UserReview saved = userReviewDao.save(createUnsavedReview(reviewedUser, reviewer, "A review."));
            Optional<UserReview> found = userReviewDao.findById(saved.getId());
            assertTrue(found.isPresent());
            assertEquals(saved.getId(), found.get().getId());
        }

        @Test
        @DisplayName("Should find all reviews")
        void findAll_withData_shouldReturnAll() {
            userReviewDao.save(createUnsavedReview(reviewedUser, reviewer, "Review 1"));
            userReviewDao.save(createUnsavedReview(reviewer, reviewedUser, "Review 2"));
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
            UserReview saved = userReviewDao.save(createUnsavedReview(reviewedUser, reviewer, "To delete."));
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
            userReviewDao.save(createUnsavedReview(reviewedUser, reviewer, "Review for user 1"));
            userReviewDao.save(createUnsavedReview(reviewer, reviewedUser, "Review for user 2"));

            UserReviewFilter filter = UserReviewFilter.ofUserId(reviewedUser.getId());
            List<UserReview> results = userReviewDao.findByFilter(filter);

            assertEquals(1, results.size());
            assertEquals(reviewedUser.getId(), results.get(0).getUser().getId());
        }

        @Test
        @DisplayName("Should find reviews by reviewer ID")
        void findByFilter_byReviewerId_shouldReturnMatching() throws SQLException {
            userReviewDao.save(createUnsavedReview(reviewedUser, reviewer, "Review by reviewer"));
            userReviewDao.save(createUnsavedReview(reviewer, reviewedUser, "Another review"));

            UserReviewFilter filter = UserReviewFilter.ofReviewerId(reviewer.getId());
            List<UserReview> results = userReviewDao.findByFilter(filter);

            assertEquals(1, results.size());
            assertEquals(reviewer.getId(), results.get(0).getReviewer().getId());
        }
    }
}