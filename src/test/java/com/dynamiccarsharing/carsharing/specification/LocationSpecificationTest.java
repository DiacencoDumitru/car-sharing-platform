package com.dynamiccarsharing.carsharing.specification;

import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.jpa.LocationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class LocationSpecificationTest {

    @Autowired
    private LocationJpaRepository locationRepository;

    @BeforeEach
    void setUp() {
        locationRepository.save(Location.builder().city("New York").state("NY").zipCode("10001").build());
        locationRepository.save(Location.builder().city("Los Angeles").state("CA").zipCode("90001").build());
        locationRepository.save(Location.builder().city("Newark").state("NJ").zipCode("07102").build());
    }

    @Test
    void cityContains_withPartialMatch_returnsMatchingLocations() {
        Specification<Location> spec = LocationSpecification.cityContains("york");
        List<Location> results = locationRepository.findAll(spec);
        assertEquals(1, results.size());
    }

    @Test
    void stateContains_withPartialMatch_returnsMatchingLocations() {
        Specification<Location> spec = LocationSpecification.stateContains("ny");
        List<Location> results = locationRepository.findAll(spec);
        assertEquals(1, results.size());
    }

    @Test
    void hasZipCode_withExactMatch_returnsMatchingLocation() {
        Specification<Location> spec = LocationSpecification.hasZipCode("90001");
        List<Location> results = locationRepository.findAll(spec);
        assertEquals(1, results.size());
    }

    @Test
    void withCriteria_withAllFields_returnsMatchingLocation() {
        Specification<Location> spec = LocationSpecification.withCriteria("Newark", "NJ", "07102");
        List<Location> results = locationRepository.findAll(spec);
        assertEquals(1, results.size());
    }
}