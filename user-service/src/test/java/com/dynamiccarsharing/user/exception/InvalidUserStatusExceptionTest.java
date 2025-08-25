package com.dynamiccarsharing.user.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InvalidUserStatusExceptionTest {

    @Test
    void constructor_withMessage_shouldSetMessage() {
        String errorMessage = "User is already suspended.";
        InvalidUserStatusException exception = new InvalidUserStatusException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }
}