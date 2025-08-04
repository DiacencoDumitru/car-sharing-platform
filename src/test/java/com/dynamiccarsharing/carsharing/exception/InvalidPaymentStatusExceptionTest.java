package com.dynamiccarsharing.carsharing.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvalidPaymentStatusExceptionTest {

    @Test
    void constructor_withMessage_shouldSetMessage() {
        String errorMessage = "Payment must be PENDING to be confirmed.";
        InvalidPaymentStatusException exception = new InvalidPaymentStatusException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }
}