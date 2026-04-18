package com.dynamiccarsharing.booking.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvalidBookingStatusExceptionTest {

    @Test
    void constructor_withMessage_shouldSetMessage() {
        String errorMessage = "Booking is already completed.";
        InvalidBookingStatusException exception = new InvalidBookingStatusException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }
}