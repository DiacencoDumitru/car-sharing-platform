package com.dynamiccarsharing.car.repository.jpa;

import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.CarType;
import com.dynamiccarsharing.contracts.enums.VerificationStatus;
import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.model.Location;
import com.dynamiccarsharing.car.specification.CarSpecification;
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
    private Car hondaCivic;
    private Car hondaAccord;
    private Car toyotaCamry;

    @BeforeEach
    void setUp() {
        savedLocation = locationRepository.save(
                Location.builder().city("Test City").state("TS").zipCode("12345").build()
        );

        hondaCivic = carRepository.save(Car.builder()
                .ownerId(200L)
                .make("Honda").model("Civic").type(CarType.SEDAN).status(CarStatus.AVAILABLE)
                .registrationNumber("HONDA1").price(new BigDecimal("50.00"))
                .location(savedLocation).verificationStatus(VerificationStatus.VERIFIED)
                .build());

        hondaAccord = carRepository.save(Car.builder()
                .ownerId(201L)
                .make("Honda").model("Accord").type(CarType.SEDAN).status(CarStatus.RENTED)
                .registrationNumber("HONDA2").price(new BigDecimal("60.00"))
                .location(savedLocation).verificationStatus(VerificationStatus.VERIFIED)
                .build());

        toyotaCamry = carRepository.save(Car.builder()
                .ownerId(202L)
                .make("Toyota").model("Camry").type(CarType.SEDAN).status(CarStatus.AVAILABLE)
                .registrationNumber("TOYOTA1").price(new BigDecimal("55.00"))
                .location(savedLocation).verificationStatus(VerificationStatus.VERIFIED)
                .build());
    }


    @Test
    void whenFilteringByMake_shouldReturnMatchingCars() {
        Specification<Car> spec = CarSpecification.hasMake("Honda");
        List<Car> results = carRepository.findAll(spec);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(c -> c.getMake().equals("Honda")));
    }

    @Test
    void whenFilteringByStatusIn_shouldReturnMatchingCars() {
        Specification<Car> spec = CarSpecification.hasStatusIn(List.of(CarStatus.AVAILABLE));
        List<Car> results = carRepository.findAll(spec);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(c -> c.getStatus() == CarStatus.AVAILABLE));
    }

    @Test
    void whenFilteringByPriceGreaterThan_shouldReturnMatchingCars() {
        Specification<Car> spec = CarSpecification.priceGreaterThan(new BigDecimal("55.00"));
        List<Car> results = carRepository.findAll(spec);
        assertEquals(2, results.size());
        assertTrue(results.contains(hondaAccord));
        assertTrue(results.contains(toyotaCamry));
    }

    @Test
    void whenFilteringByPriceLessThan_shouldReturnMatchingCars() {
        Specification<Car> spec = CarSpecification.priceLessThan(new BigDecimal("55.00"));
        List<Car> results = carRepository.findAll(spec);
        assertEquals(2, results.size());
        assertTrue(results.contains(hondaCivic));
        assertTrue(results.contains(toyotaCamry));
    }

    @Test
    void whenFilteringWithCriteria_shouldReturnMatchingCars() {
        Specification<Car> spec = CarSpecification.withCriteria("Honda", null, List.of(CarStatus.AVAILABLE), savedLocation.getId(), CarType.SEDAN, new BigDecimal("49.00"), new BigDecimal("51.00"), VerificationStatus.VERIFIED, null);
        List<Car> results = carRepository.findAll(spec);
        assertEquals(1, results.size());
        assertEquals("Civic", results.get(0).getModel());
    }

    @Test
    void whenFilteringByOwnerId_shouldReturnOnlyThatOwnersCars() {
        Specification<Car> spec = CarSpecification.hasOwnerId(200L);
        List<Car> results = carRepository.findAll(spec);
        assertEquals(1, results.size());
        assertEquals(200L, results.get(0).getOwnerId());
    }
}