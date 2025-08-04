package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class DisputeMapperTest {

    private final DisputeMapper disputeMapper = Mappers.getMapper(DisputeMapper.class);

    @Test
    void mapBooking_withValidId_shouldReturnBookingWithId() {
        Long bookingId = 30L;

        Booking result = disputeMapper.mapBooking(bookingId);

        assertNotNull(result);
        assertEquals(bookingId, result.getId());
    }

    @Test
    void mapBooking_withNullId_shouldReturnNull() {
        Booking result = disputeMapper.mapBooking(null);

        assertNull(result);
    }

    @Test
    void mapUser_withValidId_shouldReturnUserWithId() {
        Long userId = 40L;

        User result = disputeMapper.mapUser(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
    }

    @Test
    void mapUser_withNullId_shouldReturnNull() {
        User result = disputeMapper.mapUser(null);

        assertNull(result);
    }
}