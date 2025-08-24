package com.dynamiccarsharing.user.repository.jpa;

import com.dynamiccarsharing.user.filter.UserReviewFilter;
import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.user.model.UserReview;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.sql.SQLException;
import java.util.List;

import static com.dynamiccarsharing.contracts.enums.UserRole.RENTER;
import static com.dynamiccarsharing.contracts.enums.UserStatus.ACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class UserReviewJpaRepositoryTest {

    @Autowired
    private UserReviewJpaRepository userReviewRepository;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private ContactInfoJpaRepository contactInfoRepository;

    private User user1;
    private User reviewer;

    @BeforeEach
    void setUp() {
        ContactInfo ci1 = contactInfoRepository.save(ContactInfo.builder().email("user1@test.com").firstName("a").lastName("b").password("password123").phoneNumber("1").build());
        ContactInfo ci2 = contactInfoRepository.save(ContactInfo.builder().email("reviewer@test.com").firstName("e").lastName("f").password("password123").phoneNumber("3").build());

        user1 = userRepository.save(User.builder().role(RENTER).status(ACTIVE).contactInfo(ci1).build());
        reviewer = userRepository.save(User.builder().role(RENTER).status(ACTIVE).contactInfo(ci2).build());
        userReviewRepository.save(UserReview.builder().user(user1).reviewer(reviewer).comment("Good").build());
    }

    @Test
    void findByFilter_withCriteria_returnsMatchingReview() throws SQLException {
        UserReviewFilter filter = UserReviewFilter.of(user1.getId(), reviewer.getId(), "");
        List<UserReview> results = userReviewRepository.findByFilter(filter);
        assertEquals(1, results.size());
    }
}