package com.dynamiccarsharing.carsharing.criteria;

import com.dynamiccarsharing.carsharing.dto.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BookingSearchCriteriaTest {

    @Test
    void builder_withAllFields_setsAndGetsAllFields() {
        Long renterId = 1L;
        Long carId = 10L;
        TransactionStatus status = TransactionStatus.PENDING;

        BookingSearchCriteria criteria = BookingSearchCriteria.builder()
                .renterId(renterId)
                .carId(carId)
                .status(status)
                .build();

        assertNotNull(criteria);
        assertEquals(renterId, criteria.getRenterId());
        assertEquals(carId, criteria.getCarId());
        assertEquals(status, criteria.getStatus());
    }
}