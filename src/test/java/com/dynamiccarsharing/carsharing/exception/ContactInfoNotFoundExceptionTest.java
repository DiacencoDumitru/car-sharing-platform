package com.dynamiccarsharing.carsharing.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContactInfoNotFoundExceptionTest {

    @Test
    void constructor_withMessage_shouldSetMessage() {
        String errorMessage = "Contact info with ID 1 not found.";
        ContactInfoNotFoundException exception = new ContactInfoNotFoundException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void constructor_withMessageAndCause_shouldSetBoth() {
        String errorMessage = "Contact info with ID 1 not found.";
        Throwable cause = new RuntimeException("A cause");
        ContactInfoNotFoundException exception = new ContactInfoNotFoundException(errorMessage, cause);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}