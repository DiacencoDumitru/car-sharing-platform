package com.dynamiccarsharing.carsharing.criteria;

import com.dynamiccarsharing.carsharing.dto.criteria.LocationSearchCriteria;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LocationSearchCriteriaTest {
    
    @Test
    void builder_withAllFields_setsAndGetsAllFields() {
        String city = "New York";
        String state = "NY";
        String zipCode = "10001";

        LocationSearchCriteria criteria = LocationSearchCriteria.builder()
                .city(city)
                .state(state)
                .zipCode(zipCode)
                .build();

        assertNotNull(criteria);
        assertEquals(city, criteria.getCity());
        assertEquals(state, criteria.getState());
        assertEquals(zipCode, criteria.getZipCode());
    }
}