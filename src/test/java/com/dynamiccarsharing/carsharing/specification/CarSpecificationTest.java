package com.dynamiccarsharing.carsharing.specification;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.jpa.InternalCarJpaRepository;
import com.dynamiccarsharing.carsharing.repository.jpa.LocationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class CarSpecificationTest {

    @Autowired
    private InternalCarJpaRepository carRepository;

    @Autowired
    private LocationJpaRepository locationRepository;

    private Location savedLocation;

    @BeforeEach
    void setUp() {
        savedLocation = locationRepository.save(Location.builder().city("Test City").state("TS").zipCode("12345").build());

        carRepository.save(Car.builder()
                .make("Honda").model("Civic").type(CarType.SEDAN).status(CarStatus.AVAILABLE)
                .registrationNumber("HONDA1").price(new BigDecimal("50.00")).location(savedLocation)
                .verificationStatus(VerificationStatus.VERIFIED).build());

        carRepository.save(Car.builder()
                .make("Honda").model("Accord").type(CarType.SEDAN).status(CarStatus.RENTED)
                .registrationNumber("HONDA2").price(new BigDecimal("60.00")).location(savedLocation)
                .verificationStatus(VerificationStatus.VERIFIED).build());

        carRepository.save(Car.builder()
                .make("Toyota").model("Camry").type(CarType.SEDAN).status(CarStatus.AVAILABLE)
                .registrationNumber("TOYOTA1").price(new BigDecimal("55.00")).location(savedLocation)
                .verificationStatus(VerificationStatus.VERIFIED).build());
    }

    @Test
    void whenFilteringByMake_shouldReturnMatchingCars() {
        Specification<Car> spec = CarSpecification.hasMake("Honda");
        List<Car> results = carRepository.findAll(spec);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(c -> c.getMake().equals("Honda")));
    }

    @Test
    void whenFilteringWithCriteria_shouldReturnMatchingCars() {
        Specification<Car> spec = CarSpecification.withCriteria("Honda", null, List.of(CarStatus.AVAILABLE), savedLocation.getId(), CarType.SEDAN, null, null, null);
        List<Car> results = carRepository.findAll(spec);
        assertEquals(1, results.size());
        assertEquals("Civic", results.get(0).getModel());
    }
}