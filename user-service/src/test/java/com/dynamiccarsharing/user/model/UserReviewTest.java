package com.dynamiccarsharing.user.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserReviewTest {

    private User testUser = User.builder().id(1L).build();
    private User testReviewer = User.builder().id(2L).build();

    private UserReview createUserReview() {
        return UserReview.builder()
                .id(100L)
                .user(testUser)
                .reviewer(testReviewer)
                .comment("Great user!")
                .build();
    }

    @Test
    void testAllArgsConstructor() {
        UserReview review = new UserReview(101L, testUser, testReviewer, "OK");

        assertEquals(101L, review.getId());
        assertEquals(testUser, review.getUser());
        assertEquals(testReviewer, review.getReviewer());
        assertEquals("OK", review.getComment());
    }

    @Test
    void testBuilderAndGetters() {
        UserReview review = createUserReview();
        
        assertEquals(100L, review.getId());
        assertEquals(testUser, review.getUser());
        assertEquals(testReviewer, review.getReviewer());
        assertEquals("Great user!", review.getComment());
    }
    
    @Test
    void testSetters() {
        UserReview review = UserReview.builder().build();
        
        review.setId(102L);
        review.setUser(testUser);
        review.setReviewer(testReviewer);
        review.setComment("New comment");

        assertEquals(102L, review.getId());
        assertEquals(testUser, review.getUser());
        assertEquals(testReviewer, review.getReviewer());
        assertEquals("New comment", review.getComment());
    }

    @Test
    void testToBuilder() {
        UserReview review1 = createUserReview();
        UserReview review2 = review1.toBuilder().comment("Updated comment").build();
        
        assertEquals(100L, review2.getId());
        assertEquals(testUser, review2.getUser());
        assertEquals("Updated comment", review2.getComment());
    }

    @Test
    void testToString() {
        UserReview review = createUserReview();
        String s = review.toString();
        
        assertTrue(s.contains("id=100"));
        assertTrue(s.contains("comment=Great user!"));
    }

    @Test
    void testEqualsAndHashCode_BranchCoverage() {
        UserReview review1 = createUserReview();
        UserReview review2 = createUserReview();
        
        assertEquals(review1, review1);
        assertEquals(review1, review2);
        assertEquals(review1.hashCode(), review2.hashCode());
        
        assertNotEquals(review1, null);
        assertNotEquals(review1, new Object());
        
        review2 = review1.toBuilder().id(200L).build();
        assertNotEquals(review1, review2);
        assertNotEquals(review1.hashCode(), review2.hashCode());

        review2 = review1.toBuilder().user(User.builder().id(5L).build()).build();
        assertNotEquals(review1, review2);
        
        review2 = review1.toBuilder().comment("Bad user").build();
        assertNotEquals(review1, review2);
        
        review2 = review1.toBuilder().reviewer(User.builder().id(5L).build()).build();
        
        assertEquals(review1, review2);
        assertEquals(review1.hashCode(), review2.hashCode());
    }
}