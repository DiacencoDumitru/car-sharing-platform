package com.dynamiccarsharing.car.repository.jpa;

import com.dynamiccarsharing.car.criteria.CarSearchCriteria;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.car.filter.CarFilter;
import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.model.Location;
import com.dynamiccarsharing.car.repository.CarRepository;
import com.dynamiccarsharing.car.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static com.dynamiccarsharing.contracts.enums.CarType.SEDAN;
import static com.dynamiccarsharing.contracts.enums.VerificationStatus.VERIFIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({CarJpaRepositoryImpl.class})
class CarJpaRepositoryImplTest {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private LocationRepository locationRepository;

    private Location savedLocation;

    @BeforeEach
    void setUp() {
        savedLocation = locationRepository.save(
                Location.builder().city("Test").state("TS").zipCode("123").build()
        );
        carRepository.save(Car.builder().ownerId(300L).make("Honda").model("Civic").status(CarStatus.AVAILABLE).verificationStatus(VERIFIED).registrationNumber("CAR1").price(new BigDecimal("20.00")).type(SEDAN).location(savedLocation).averageRating(new BigDecimal("4.50")).reviewCount(2).build());
        carRepository.save(Car.builder().ownerId(301L).make("Toyota").model("Camry").status(CarStatus.RENTED).verificationStatus(VERIFIED).registrationNumber("CAR2").price(new BigDecimal("25.00")).type(SEDAN).location(savedLocation).averageRating(new BigDecimal("3.00")).reviewCount(1).build());
        carRepository.save(Car.builder().ownerId(302L).make("Honda").model("Accord").status(CarStatus.MAINTENANCE).verificationStatus(VERIFIED).registrationNumber("CAR3").price(new BigDecimal("30.00")).type(SEDAN).location(savedLocation).reviewCount(0).build());
    }


    @Test
    void findByFilter_withCriteria_returnsMatchingCars() throws SQLException {
        CarFilter filter = CarFilter.of("Honda", "Civic", CarStatus.AVAILABLE, savedLocation, SEDAN, VERIFIED);

        List<Car> results = carRepository.findByFilter(filter);

        assertEquals(1, results.size());
        assertEquals("Honda", results.get(0).getMake());
        assertEquals("Civic", results.get(0).getModel());
    }

    @Test
    void findAll_withComplexCriteria_returnsMatchingCars() {
        CarSearchCriteria criteria = CarSearchCriteria.builder()
                .make("Honda")
                .statusIn(List.of(CarStatus.AVAILABLE, CarStatus.MAINTENANCE))
                .minPrice(new BigDecimal("19.00"))
                .maxPrice(new BigDecimal("31.00"))
                .build();

        Page<Car> results = carRepository.findAll(criteria, PageRequest.of(0, 10));

        assertEquals(2, results.getTotalElements());
        assertTrue(results.getContent().stream().allMatch(c -> c.getMake().equals("Honda")));
        assertTrue(results.getContent().stream().anyMatch(c -> c.getModel().equals("Civic")));
        assertTrue(results.getContent().stream().anyMatch(c -> c.getModel().equals("Accord")));
    }

    @Test
    void findAll_withMinAverageRating_excludesNullAndBelowThreshold() {
        CarSearchCriteria criteria = CarSearchCriteria.builder()
                .minAverageRating(new BigDecimal("4.00"))
                .build();

        Page<Car> results = carRepository.findAll(criteria, PageRequest.of(0, 10));

        assertEquals(1, results.getTotalElements());
        assertEquals("Civic", results.getContent().get(0).getModel());
    }

    @Test
    void findAll_withMinAverageRating_includesAllRatedCarsMeetingThreshold() {
        CarSearchCriteria criteria = CarSearchCriteria.builder()
                .minAverageRating(new BigDecimal("3.00"))
                .build();

        Page<Car> results = carRepository.findAll(criteria, PageRequest.of(0, 10));

        assertEquals(2, results.getTotalElements());
        assertTrue(results.getContent().stream().anyMatch(c -> c.getModel().equals("Civic")));
        assertTrue(results.getContent().stream().anyMatch(c -> c.getModel().equals("Camry")));
    }

    @Test
    void findAll_withMinReviewCount_filtersByReviewCount() {
        CarSearchCriteria criteria = CarSearchCriteria.builder()
                .minReviewCount(2)
                .build();

        Page<Car> results = carRepository.findAll(criteria, PageRequest.of(0, 10));

        assertEquals(1, results.getTotalElements());
        assertEquals("Civic", results.getContent().get(0).getModel());
    }

    @Test
    void findAll_withMinReviewCount_treatsNullReviewCountAsZero() {
        CarSearchCriteria criteria = CarSearchCriteria.builder()
                .minReviewCount(0)
                .build();

        Page<Car> results = carRepository.findAll(criteria, PageRequest.of(0, 10));

        assertEquals(3, results.getTotalElements());
    }
}