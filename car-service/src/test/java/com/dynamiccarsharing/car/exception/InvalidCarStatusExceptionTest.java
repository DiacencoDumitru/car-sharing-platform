package com.dynamiccarsharing.car.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvalidCarStatusExceptionTest {

    @Test
    void constructor_withMessage_shouldSetMessage() {
        String errorMessage = "Car must be AVAILABLE to be rented.";
        InvalidCarStatusException exception = new InvalidCarStatusException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }
}