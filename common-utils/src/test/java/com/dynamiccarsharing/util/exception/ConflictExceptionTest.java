package com.dynamiccarsharing.util.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConflictExceptionTest {

    @Test
    @DisplayName("Constructor with message only")
    void constructor_withMessage_setsMessage() {
        String message = "Resource conflict occurred.";
        ConflictException ex = new ConflictException(message);

        assertEquals(message, ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    @DisplayName("Constructor with message and cause")
    void constructor_withMessageAndCause_setsBoth() {
        String message = "Operation conflict";
        Throwable cause = new IllegalStateException("Invalid state");
        ConflictException ex = new ConflictException(message, cause);

        assertEquals(message, ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}