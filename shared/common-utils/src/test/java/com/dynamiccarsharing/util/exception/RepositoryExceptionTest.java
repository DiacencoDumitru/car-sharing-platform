package com.dynamiccarsharing.util.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryExceptionTest {

    @Test
    void constructor_withMessageAndCause_initializesCorrectly() {
        String message = "Database error";
        Throwable cause = new RuntimeException("Underlying issue");
        RepositoryException ex = new RepositoryException(message, cause);

        assertEquals(message, ex.getMessage());
        assertEquals(cause, ex.getCause());
    }
}