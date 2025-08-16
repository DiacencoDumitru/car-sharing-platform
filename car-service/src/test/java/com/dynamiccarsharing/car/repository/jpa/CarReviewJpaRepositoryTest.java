package com.dynamiccarsharing.car.repository.jpa;

import com.dynamiccarsharing.car.filter.CarReviewFilter;
import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.model.CarReview;
import com.dynamiccarsharing.car.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static com.dynamiccarsharing.contracts.enums.CarStatus.AVAILABLE;
import static com.dynamiccarsharing.contracts.enums.CarType.SEDAN;
import static com.dynamiccarsharing.contracts.enums.VerificationStatus.VERIFIED;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class CarReviewJpaRepositoryTest {

    @Autowired
    private CarReviewJpaRepository carReviewRepository;
    @Autowired
    private InternalCarJpaRepository carRepository;
    @Autowired
    private LocationJpaRepository locationRepository;

    private Car car1;
    private Long reviewerId1;

    @BeforeEach
    void setUp() {
        reviewerId1 = 1L;

        Location location = locationRepository.save(Location.builder().city("Test").state("TS").zipCode("123").build());
        car1 = carRepository.save(Car.builder().make("Honda").model("Civic").status(AVAILABLE).verificationStatus(VERIFIED).registrationNumber("CAR1").price(BigDecimal.TEN).type(SEDAN).location(location).build());

        carReviewRepository.save(CarReview.builder().car(car1).reviewerId(reviewerId1).comment("Good").build());
    }

    @Test
    void findByFilter_withCriteria_returnsMatchingReviews() throws SQLException {
        CarReviewFilter filter = CarReviewFilter.of(reviewerId1, car1.getId());

        List<CarReview> results = carReviewRepository.findByFilter(filter);

        assertEquals(1, results.size());
        assertEquals(reviewerId1, results.get(0).getReviewerId());
    }
}