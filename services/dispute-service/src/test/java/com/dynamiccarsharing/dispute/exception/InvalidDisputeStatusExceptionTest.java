package com.dynamiccarsharing.dispute.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvalidDisputeStatusExceptionTest {

    @Test
    void constructor_withMessage_shouldSetMessage() {
        String errorMessage = "Dispute is already resolved.";
        InvalidDisputeStatusException exception = new InvalidDisputeStatusException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }
}