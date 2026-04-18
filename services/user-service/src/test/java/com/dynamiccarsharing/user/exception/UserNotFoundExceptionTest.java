package com.dynamiccarsharing.user.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserNotFoundExceptionTest {

    @Test
    void constructor_withMessage_shouldSetMessage() {
        String errorMessage = "User with ID 1 not found.";
        UserNotFoundException exception = new UserNotFoundException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void constructor_withMessageAndCause_shouldSetBoth() {
        String errorMessage = "User with ID 1 not found.";
        Throwable cause = new RuntimeException("Some cause");
        UserNotFoundException exception = new UserNotFoundException(errorMessage, cause);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}