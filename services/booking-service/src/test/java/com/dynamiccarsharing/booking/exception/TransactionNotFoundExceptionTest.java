package com.dynamiccarsharing.booking.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionNotFoundExceptionTest {

    @Test
    void constructor_withMessage_shouldSetMessage() {
        String errorMessage = "Transaction with ID 789 not found.";
        TransactionNotFoundException exception = new TransactionNotFoundException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void constructor_withMessageAndCause_shouldSetBoth() {
        String errorMessage = "Transaction with ID 789 not found.";
        Throwable cause = new RuntimeException("DB error");
        TransactionNotFoundException exception = new TransactionNotFoundException(errorMessage, cause);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}