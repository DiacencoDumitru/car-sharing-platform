package com.dynamiccarsharing.carsharing.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvalidCarStatusExceptionTest {

    @Test
    void constructor_withMessage_shouldSetMessage() {
        String errorMessage = "Car must be AVAILABLE to be rented.";
        InvalidCarStatusException exception = new InvalidCarStatusException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }
}