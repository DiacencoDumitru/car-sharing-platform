package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.filter.CarReviewFilter;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static com.dynamiccarsharing.carsharing.enums.CarStatus.AVAILABLE;
import static com.dynamiccarsharing.carsharing.enums.CarType.SEDAN;
import static com.dynamiccarsharing.carsharing.enums.UserRole.RENTER;
import static com.dynamiccarsharing.carsharing.enums.UserStatus.ACTIVE;
import static com.dynamiccarsharing.carsharing.enums.VerificationStatus.VERIFIED;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class CarReviewJpaRepositoryTest {

    @Autowired
    private CarReviewJpaRepository carReviewRepository;
    @Autowired
    private CarJpaRepository carRepository;
    @Autowired
    private UserJpaRepository userRepository;
    @Autowired
    private LocationJpaRepository locationRepository;

    private User reviewer1;
    private Car car1;

    @BeforeEach
    void setUp() {
        Location location = locationRepository.save(Location.builder().city("Test").state("TS").zipCode("123").build());
        car1 = carRepository.save(Car.builder().make("Honda").model("Civic").status(AVAILABLE).verificationStatus(VERIFIED).registrationNumber("CAR1").price(BigDecimal.TEN).type(SEDAN).location(location).build());
        reviewer1 = userRepository.save(User.builder().role(RENTER).status(ACTIVE).build());
        
        carReviewRepository.save(CarReview.builder().car(car1).reviewer(reviewer1).comment("Good").build());
    }
    
    @Test
    void findByFilter_withCriteria_returnsMatchingReviews() throws SQLException {
        CarReviewFilter filter = CarReviewFilter.of(reviewer1.getId(), car1.getId());
        
        List<CarReview> results = carReviewRepository.findByFilter(filter);
        
        assertEquals(1, results.size());
    }
}