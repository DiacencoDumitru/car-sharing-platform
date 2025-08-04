package com.dynamiccarsharing.carsharing.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvalidVerificationStatusExceptionTest {

    @Test
    void constructor_withMessage_shouldSetMessage() {
        String errorMessage = "Car has already been verified.";
        InvalidVerificationStatusException exception = new InvalidVerificationStatusException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }
}