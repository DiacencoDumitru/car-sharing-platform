package com.dynamiccarsharing.carsharing.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvalidDisputeStatusExceptionTest {

    @Test
    void constructor_withMessage_shouldSetMessage() {
        String errorMessage = "Dispute is already resolved.";
        InvalidDisputeStatusException exception = new InvalidDisputeStatusException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }
}