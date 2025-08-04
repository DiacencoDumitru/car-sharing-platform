package com.dynamiccarsharing.carsharing.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserReviewNotFoundExceptionTest {

    @Test
    void constructor_withMessage_shouldSetMessage() {
        String errorMessage = "User review with ID 2 not found.";
        UserReviewNotFoundException exception = new UserReviewNotFoundException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void constructor_withMessageAndCause_shouldSetBoth() {
        String errorMessage = "User review with ID 2 not found.";
        Throwable cause = new RuntimeException("Another cause");
        UserReviewNotFoundException exception = new UserReviewNotFoundException(errorMessage, cause);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}