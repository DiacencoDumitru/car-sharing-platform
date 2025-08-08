package com.dynamiccarsharing.carsharing.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvalidPriceExceptionTest {

    @Test
    void constructor_withMessage_shouldSetMessage() {
        String errorMessage = "Price cannot be negative.";
        InvalidPriceException exception = new InvalidPriceException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }
}