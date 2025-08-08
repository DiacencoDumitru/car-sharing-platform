package com.dynamiccarsharing.carsharing.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvalidBookingStatusExceptionTest {

    @Test
    void constructor_withMessage_shouldSetMessage() {
        String errorMessage = "Booking is already completed.";
        InvalidBookingStatusException exception = new InvalidBookingStatusException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }
}