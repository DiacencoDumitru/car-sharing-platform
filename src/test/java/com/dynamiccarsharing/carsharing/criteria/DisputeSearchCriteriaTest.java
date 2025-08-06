package com.dynamiccarsharing.carsharing.criteria;

import com.dynamiccarsharing.carsharing.dto.criteria.DisputeSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DisputeSearchCriteriaTest {
    
    @Test
    void builder_withAllFields_setsAndGetsAllFields() {
        Long bookingId = 1L;
        DisputeStatus status = DisputeStatus.OPEN;

        DisputeSearchCriteria criteria = DisputeSearchCriteria.builder()
                .bookingId(bookingId)
                .status(status)
                .build();

        assertNotNull(criteria);
        assertEquals(bookingId, criteria.getBookingId());
        assertEquals(status, criteria.getStatus());
    }
}