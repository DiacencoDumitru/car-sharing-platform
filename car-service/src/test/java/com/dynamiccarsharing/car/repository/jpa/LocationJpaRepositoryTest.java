package com.dynamiccarsharing.car.repository.jpa;

import com.dynamiccarsharing.car.filter.LocationFilter;
import com.dynamiccarsharing.car.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class LocationJpaRepositoryTest {

    @Autowired
    private LocationJpaRepository locationRepository;

    @BeforeEach
    void setUp() {
        locationRepository.save(Location.builder().city("New York").state("NY").zipCode("10001").build());
        locationRepository.save(Location.builder().city("Boston").state("MA").zipCode("02101").build());
    }

    @Test
    void findByFilter_withCriteria_returnsMatchingLocation() throws SQLException {
        LocationFilter filter = LocationFilter.of("10001", "NY", "New York");

        List<Location> results = locationRepository.findByFilter(filter);

        assertEquals(1, results.size());
        assertEquals("New York", results.get(0).getCity());
        assertEquals("10001", results.get(0).getZipCode());
    }
}