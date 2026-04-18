package com.dynamiccarsharing.car.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocationNotFoundExceptionTest {

    @Test
    void constructor_withMessage_shouldSetMessage() {
        String errorMessage = "Location with ID 123 not found.";
        LocationNotFoundException exception = new LocationNotFoundException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void constructor_withMessageAndCause_shouldSetBoth() {
        String errorMessage = "Location with ID 123 not found.";
        Throwable cause = new RuntimeException("Database connection failed");
        LocationNotFoundException exception = new LocationNotFoundException(errorMessage, cause);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}