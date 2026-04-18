package com.dynamiccarsharing.util.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceExceptionTest {

    @Test
    void constructor_withMessageAndCause_initializesCorrectly() {
        String message = "Service layer error";
        Throwable cause = new IllegalArgumentException("Invalid input");
        ServiceException ex = new ServiceException(message, cause);

        assertEquals(message, ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}