package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.model.Booking;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class PaymentMapperTest {

    private final PaymentMapper paymentMapper = Mappers.getMapper(PaymentMapper.class);

    @Test
    void map_withValidBookingId_shouldReturnBookingWithId() {
        Long bookingId = 50L;

        Booking result = paymentMapper.map(bookingId);

        assertNotNull(result);
        assertEquals(bookingId, result.getId());
    }

    @Test
    void map_withNullBookingId_shouldReturnNull() {
        Booking result = paymentMapper.map(null);

        assertNull(result);
    }
}