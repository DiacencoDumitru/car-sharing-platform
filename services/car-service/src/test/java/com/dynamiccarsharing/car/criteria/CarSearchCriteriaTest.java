package com.dynamiccarsharing.car.criteria;

import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.CarType;
import com.dynamiccarsharing.contracts.enums.VerificationStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CarSearchCriteriaTest {

    @Test
    void builder_withAllFields_setsAndGetsAllFields() {
        String make = "Honda";
        String model = "Civic";
        CarStatus status = CarStatus.AVAILABLE;
        Long locationId = 5L;
        CarType type = CarType.SEDAN;
        VerificationStatus verificationStatus = VerificationStatus.VERIFIED;

        Long ownerId = 77L;
        CarSearchCriteria criteria = CarSearchCriteria.builder()
                .make(make)
                .model(model)
                .ownerId(ownerId)
                .statusIn(List.of(status))
                .locationId(locationId)
                .type(type)
                .verificationStatus(verificationStatus)
                .build();

        assertNotNull(criteria);
        assertEquals(make, criteria.getMake());
        assertEquals(model, criteria.getModel());
        assertEquals(ownerId, criteria.getOwnerId());
        assertEquals(List.of(status), criteria.getStatusIn());
        assertEquals(locationId, criteria.getLocationId());
        assertEquals(type, criteria.getType());
        assertEquals(verificationStatus, criteria.getVerificationStatus());
    }
}