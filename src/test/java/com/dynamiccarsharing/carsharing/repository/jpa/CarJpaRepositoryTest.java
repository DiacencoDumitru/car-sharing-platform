package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.filter.CarFilter;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static com.dynamiccarsharing.carsharing.enums.CarType.SEDAN;
import static com.dynamiccarsharing.carsharing.enums.VerificationStatus.VERIFIED;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class CarJpaRepositoryTest {

    @Autowired
    private CarJpaRepository carRepository;
    @Autowired
    private LocationJpaRepository locationRepository;

    private Location savedLocation;

    @BeforeEach
    void setUp() {
        savedLocation = locationRepository.save(Location.builder().city("Test").state("TS").zipCode("123").build());
        carRepository.save(Car.builder().make("Honda").model("Civic").status(CarStatus.AVAILABLE).verificationStatus(VERIFIED).registrationNumber("CAR1").price(BigDecimal.TEN).type(SEDAN).location(savedLocation).build());
        carRepository.save(Car.builder().make("Toyota").model("Camry").status(CarStatus.RENTED).verificationStatus(VERIFIED).registrationNumber("CAR2").price(BigDecimal.TEN).type(SEDAN).location(savedLocation).build());
    }

    @Test
    void findByFilter_withCriteria_returnsMatchingCars() throws SQLException {
        CarFilter filter = CarFilter.of("Honda", "Civic", CarStatus.AVAILABLE, savedLocation, SEDAN, VERIFIED);

        List<Car> results = carRepository.findByFilter(filter);

        assertEquals(1, results.size());
        assertEquals("Honda", results.get(0).getMake());
    }
}