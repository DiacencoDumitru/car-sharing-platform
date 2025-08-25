package com.dynamiccarsharing.car.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CarNotFoundExceptionTest {

    @Test
    void constructor_withMessage_shouldSetMessage() {
        String errorMessage = "Car with ID 456 not found.";
        CarNotFoundException exception = new CarNotFoundException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void constructor_withMessageAndCause_shouldSetBoth() {
        String errorMessage = "Car with ID 456 not found.";
        Throwable cause = new RuntimeException("Some database error");
        CarNotFoundException exception = new CarNotFoundException(errorMessage, cause);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}