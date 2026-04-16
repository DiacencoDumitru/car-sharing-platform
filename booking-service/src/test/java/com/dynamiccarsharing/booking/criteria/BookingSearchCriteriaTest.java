package com.dynamiccarsharing.booking.criteria;

import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingSearchCriteriaTest {

    private final Long renterId = 1L;
    private final Long carId = 10L;
    private final TransactionStatus status = TransactionStatus.PENDING;
    private final LocalDateTime startTime = LocalDateTime.of(2023, 1, 1, 10, 0);
    private final LocalDateTime endTime = LocalDateTime.of(2023, 1, 1, 12, 0);

    @Test
    void builder_withAllFields_setsAndGetsAllFields() {
        BookingSearchCriteria criteria = BookingSearchCriteria.builder()
                .renterId(renterId)
                .carId(carId)
                .status(status)
                .startTimeAfter(startTime)
                .endTimeBefore(endTime)
                .build();

        assertNotNull(criteria);
        assertEquals(renterId, criteria.getRenterId());
        assertEquals(carId, criteria.getCarId());
        assertEquals(status, criteria.getStatus());
        assertEquals(startTime, criteria.getStartTimeAfter());
        assertEquals(endTime, criteria.getEndTimeBefore());
    }

    @Test
    void noArgsConstructor_createsEmptyObject() {
        BookingSearchCriteria criteria = new BookingSearchCriteria();

        assertNotNull(criteria);
        assertNull(criteria.getRenterId());
        assertNull(criteria.getCarId());
        assertNull(criteria.getStatus());
        assertNull(criteria.getStartTimeAfter());
        assertNull(criteria.getEndTimeBefore());
    }

    @Test
    void allArgsConstructor_createsPopulatedObject() {
        BookingSearchCriteria criteria = new BookingSearchCriteria(
                renterId, carId, null, status, startTime, endTime
        );

        assertNotNull(criteria);
        assertEquals(renterId, criteria.getRenterId());
        assertEquals(carId, criteria.getCarId());
        assertEquals(status, criteria.getStatus());
        assertEquals(startTime, criteria.getStartTimeAfter());
        assertEquals(endTime, criteria.getEndTimeBefore());
    }

    @Test
    void setters_updateFields() {
        BookingSearchCriteria criteria = new BookingSearchCriteria();

        criteria.setRenterId(renterId);
        criteria.setCarId(carId);
        criteria.setStatus(status);
        criteria.setStartTimeAfter(startTime);
        criteria.setEndTimeBefore(endTime);

        assertEquals(renterId, criteria.getRenterId());
        assertEquals(carId, criteria.getCarId());
        assertEquals(status, criteria.getStatus());
        assertEquals(startTime, criteria.getStartTimeAfter());
        assertEquals(endTime, criteria.getEndTimeBefore());
    }

    @Test
    void equalsAndHashCode_workAsExpected() {
        BookingSearchCriteria criteria1 = new BookingSearchCriteria(
                renterId, carId, null, status, startTime, endTime
        );
        BookingSearchCriteria criteria2 = new BookingSearchCriteria(
                renterId, carId, null, status, startTime, endTime
        );

        assertEquals(criteria1, criteria2);
        assertEquals(criteria1.hashCode(), criteria2.hashCode());
    }

    @Test
    void toString_containsAllFieldValues() {
        BookingSearchCriteria criteria = new BookingSearchCriteria(
                renterId, carId, null, status, startTime, endTime
        );
        
        String criteriaString = criteria.toString();

        assertNotNull(criteriaString);
        assertTrue(criteriaString.contains("renterId=1"));
        assertTrue(criteriaString.contains("carId=10"));
        assertTrue(criteriaString.contains("status=PENDING"));
        assertTrue(criteriaString.contains("startTimeAfter=2023-01-01T10:00"));
        assertTrue(criteriaString.contains("endTimeBefore=2023-01-01T12:00"));
    }
}