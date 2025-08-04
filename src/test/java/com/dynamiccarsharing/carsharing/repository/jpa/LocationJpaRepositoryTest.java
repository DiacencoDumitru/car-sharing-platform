package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.filter.LocationFilter;
import com.dynamiccarsharing.carsharing.model.Location;
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
    }

    @Test
    void findByFilter_withCriteria_returnsMatchingLocation() throws SQLException {
        LocationFilter filter = LocationFilter.of("10001", "NY", "New York");

        List<Location> results = locationRepository.findByFilter(filter);

        assertEquals(1, results.size());
    }
}