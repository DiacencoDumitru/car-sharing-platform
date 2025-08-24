package com.dynamiccarsharing.user.specification;

import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.user.model.UserReview;
import com.dynamiccarsharing.user.repository.jpa.ContactInfoJpaRepository;
import com.dynamiccarsharing.user.repository.jpa.UserJpaRepository;
import com.dynamiccarsharing.user.repository.jpa.UserReviewJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static com.dynamiccarsharing.contracts.enums.UserRole.RENTER;
import static com.dynamiccarsharing.contracts.enums.UserStatus.ACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class UserReviewSpecificationTest {

    @Autowired
    private UserReviewJpaRepository reviewRepository;
    @Autowired
    private UserJpaRepository userRepository;
    @Autowired
    private ContactInfoJpaRepository contactInfoRepository;

    private User user1, user2, reviewer;

    @BeforeEach
    void setUp() {
        ContactInfo ci1 = contactInfoRepository.save(ContactInfo.builder().email("user1@test.com").firstName("a").lastName("b").password("pass123").phoneNumber("1").build());
        ContactInfo ci2 = contactInfoRepository.save(ContactInfo.builder().email("user2@test.com").firstName("c").lastName("d").password("pass123").phoneNumber("2").build());
        ContactInfo ci3 = contactInfoRepository.save(ContactInfo.builder().email("reviewer@test.com").firstName("e").lastName("f").password("pass123").phoneNumber("3").build());

        user1 = userRepository.save(User.builder().role(RENTER).status(ACTIVE).contactInfo(ci1).build());
        user2 = userRepository.save(User.builder().role(RENTER).status(ACTIVE).contactInfo(ci2).build());
        reviewer = userRepository.save(User.builder().role(RENTER).status(ACTIVE).contactInfo(ci3).build());

        reviewRepository.save(UserReview.builder().user(user1).reviewer(reviewer).comment("Good").build());
        reviewRepository.save(UserReview.builder().user(user2).reviewer(reviewer).comment("Great").build());
    }

    @Test
    void hasUserId_withMatchingId_returnsMatchingReview() {
        Specification<UserReview> spec = UserReviewSpecification.hasUserId(user1.getId());
        List<UserReview> results = reviewRepository.findAll(spec);
        assertEquals(1, results.size());
        assertEquals("Good", results.get(0).getComment());
    }

    @Test
    void hasReviewerId_withMatchingId_returnsAllReviewsByReviewer() {
        Specification<UserReview> spec = UserReviewSpecification.hasReviewerId(reviewer.getId());
        List<UserReview> results = reviewRepository.findAll(spec);
        assertEquals(2, results.size());
    }

    @Test
    void commentContains_withPartialMatch_returnsMatchingReview() {
        Specification<UserReview> spec = UserReviewSpecification.commentContains("rea");
        List<UserReview> results = reviewRepository.findAll(spec);
        assertEquals(1, results.size());
        assertEquals(user2.getId(), results.get(0).getUser().getId());
    }

    @Test
    void withCriteria_withAllFields_returnsMatchingReview() {
        Specification<UserReview> spec = UserReviewSpecification.withCriteria(user2.getId(), reviewer.getId());
        List<UserReview> results = reviewRepository.findAll(spec);
        assertEquals(1, results.size());
    }
}