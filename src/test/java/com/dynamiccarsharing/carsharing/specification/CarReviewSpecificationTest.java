package com.dynamiccarsharing.carsharing.specification;

import com.dynamiccarsharing.carsharing.model.*;
import com.dynamiccarsharing.carsharing.repository.jpa.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static com.dynamiccarsharing.carsharing.enums.CarStatus.AVAILABLE;
import static com.dynamiccarsharing.carsharing.enums.CarType.SEDAN;
import static com.dynamiccarsharing.carsharing.enums.UserRole.RENTER;
import static com.dynamiccarsharing.carsharing.enums.UserStatus.ACTIVE;
import static com.dynamiccarsharing.carsharing.enums.VerificationStatus.VERIFIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class CarReviewSpecificationTest {

    @Autowired
    private CarReviewJpaRepository reviewRepository;

    @Autowired
    private CarJpaRepository carRepository;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private LocationJpaRepository locationRepository;

    @Autowired
    private ContactInfoJpaRepository contactInfoRepository;

    private Car car1;
    private User reviewer1;
    private User reviewer2;

    @BeforeEach
    void setUp() {
        Location location = locationRepository.save(Location.builder().city("Test").state("TS").zipCode("123").build());
        car1 = carRepository.save(Car.builder().make("Honda").model("Civic").status(AVAILABLE).verificationStatus(VERIFIED).registrationNumber("CAR1").price(BigDecimal.TEN).type(SEDAN).location(location).build());
        Car car2 = carRepository.save(Car.builder().make("Toyota").model("Camry").status(AVAILABLE).verificationStatus(VERIFIED).registrationNumber("CAR2").price(BigDecimal.TEN).type(SEDAN).location(location).build());

        ContactInfo ci1 = contactInfoRepository.save(ContactInfo.builder().email("rev1@test.com").firstName("a").lastName("b").phoneNumber("1").build());
        ContactInfo ci2 = contactInfoRepository.save(ContactInfo.builder().email("rev2@test.com").firstName("c").lastName("d").phoneNumber("2").build());
        reviewer1 = userRepository.save(User.builder().role(RENTER).status(ACTIVE).contactInfo(ci1).build());
        reviewer2 = userRepository.save(User.builder().role(RENTER).status(ACTIVE).contactInfo(ci2).build());

        reviewRepository.save(CarReview.builder().car(car1).reviewer(reviewer1).comment("Great!").build());
        reviewRepository.save(CarReview.builder().car(car2).reviewer(reviewer1).comment("Okay.").build());
        reviewRepository.save(CarReview.builder().car(car1).reviewer(reviewer2).comment("Loved it!").build());
    }

    @Test
    void whenFilteringWithCriteria_shouldReturnMatchingReviews() {
        Specification<CarReview> spec = CarReviewSpecification.withCriteria(car1.getId(), reviewer1.getId());
        List<CarReview> results = reviewRepository.findAll(spec);
        assertEquals(1, results.size());
        assertTrue(results.stream().allMatch(r -> r.getCar().getId().equals(car1.getId()) && r.getReviewer().getId().equals(reviewer1.getId())));
    }
}