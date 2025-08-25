package com.dynamiccarsharing.car.repository.jpa;

import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.model.CarReview;
import com.dynamiccarsharing.car.model.Location;
import com.dynamiccarsharing.car.specification.CarReviewSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static com.dynamiccarsharing.contracts.enums.CarStatus.AVAILABLE;
import static com.dynamiccarsharing.contracts.enums.CarType.SEDAN;
import static com.dynamiccarsharing.contracts.enums.VerificationStatus.VERIFIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class CarReviewSpecificationTest {

    @Autowired
    private CarReviewJpaRepository reviewRepository;

    @Autowired
    private InternalCarJpaRepository carRepository;

    @Autowired
    private LocationJpaRepository locationRepository;

    private Car car1;
    private Car car2;
    private Long reviewer1Id = 1L;
    private Long reviewer2Id = 2L;


    @BeforeEach
    void setUp() {
        Location location = locationRepository.save(Location.builder().city("Test").state("TS").zipCode("123").build());
        car1 = carRepository.save(Car.builder().make("Honda").model("Civic").status(AVAILABLE).verificationStatus(VERIFIED).registrationNumber("CAR1").price(BigDecimal.TEN).type(SEDAN).location(location).build());
        car2 = carRepository.save(Car.builder().make("Toyota").model("Camry").status(AVAILABLE).verificationStatus(VERIFIED).registrationNumber("CAR2").price(BigDecimal.TEN).type(SEDAN).location(location).build());

        reviewRepository.save(CarReview.builder().car(car1).reviewerId(reviewer1Id).comment("Great!").build());
        reviewRepository.save(CarReview.builder().car(car2).reviewerId(reviewer1Id).comment("Okay.").build());
        reviewRepository.save(CarReview.builder().car(car1).reviewerId(reviewer2Id).comment("Loved it!").build());
    }

    @Test
    void whenFilteringByCarId_shouldReturnMatchingReviews() {
        Specification<CarReview> spec = CarReviewSpecification.hasCarId(car1.getId());
        List<CarReview> results = reviewRepository.findAll(spec);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(r -> r.getCar().getId().equals(car1.getId())));
    }

    @Test
    void whenFilteringByReviewerId_shouldReturnMatchingReviews() {
        Specification<CarReview> spec = CarReviewSpecification.hasReviewerId(reviewer1Id);
        List<CarReview> results = reviewRepository.findAll(spec);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(r -> r.getReviewerId().equals(reviewer1Id)));
    }

    @Test
    void whenFilteringWithCriteria_shouldReturnMatchingReviews() {
        Specification<CarReview> spec = CarReviewSpecification.withCriteria(reviewer1Id, car1.getId());
        List<CarReview> results = reviewRepository.findAll(spec);
        assertEquals(1, results.size());
        assertTrue(results.stream().allMatch(r -> r.getCar().getId().equals(car1.getId()) && r.getReviewerId().equals(reviewer1Id)));
    }
}