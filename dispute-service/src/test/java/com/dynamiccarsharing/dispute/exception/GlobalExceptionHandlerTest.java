package com.dynamiccarsharing.dispute.exception;

import com.dynamiccarsharing.dispute.exception.handler.GlobalExceptionHandler;
import com.dynamiccarsharing.util.exception.ConflictException;
import com.dynamiccarsharing.util.exception.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError = new FieldError("disputeRequest", "reason", "Reason cannot be empty");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        ProblemDetail problemDetail = exceptionHandler.handleMethodArgumentNotValidException(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Validation Failed", problemDetail.getTitle());
        assertEquals("One or more validation errors occurred.", problemDetail.getDetail());
        assertEquals(URI.create("/errors/validation-failed"), problemDetail.getType());

        Map<String, String> errors = (Map<String, String>) problemDetail.getProperties().get("errors");
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals("Reason cannot be empty", errors.get("reason"));
    }

    @Test
    void handleResourceNotFoundException() {
        String errorMessage = "User with ID 42 not found";
        ResourceNotFoundException ex = new ResourceNotFoundException(errorMessage);

        ProblemDetail problemDetail = exceptionHandler.handleResourceNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("Resource Not Found", problemDetail.getTitle());
        assertEquals(errorMessage, problemDetail.getDetail());
        assertEquals(URI.create("/errors/resource-not-found"), problemDetail.getType());
    }

    @Test
    void handleConflictException() {
        String errorMessage = "A resource with this email already exists";
        ConflictException ex = new ConflictException(errorMessage);

        ProblemDetail problemDetail = exceptionHandler.handleConflictException(ex);

        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        assertEquals("Operation Conflict", problemDetail.getTitle());
        assertEquals(errorMessage, problemDetail.getDetail());
        assertEquals(URI.create("/errors/operation-conflict"), problemDetail.getType());
    }

    @Test
    void handleGenericException() {
        Exception ex = new Exception("Some unexpected database error");

        ProblemDetail problemDetail = exceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        assertEquals("Internal Server Error", problemDetail.getTitle());
        assertEquals("An unexpected internal error occurred.", problemDetail.getDetail());
        assertEquals(URI.create("/errors/internal-server-error"), problemDetail.getType());
    }

    @Test
    void handleDisputeNotFoundException() {
        String errorMessage = "Dispute with ID 99 could not be found";
        DisputeNotFoundException ex = new DisputeNotFoundException(errorMessage);

        ProblemDetail problemDetail = exceptionHandler.handleDisputeNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("Dispute Not Found", problemDetail.getTitle());
        assertEquals(errorMessage, problemDetail.getDetail());
        assertEquals(URI.create("/errors/dispute-not-found"), problemDetail.getType());
    }

    @Test
    void handleInvalidDisputeStatusException() {
        String errorMessage = "Cannot resolve an already cancelled dispute";
        InvalidDisputeStatusException ex = new InvalidDisputeStatusException(errorMessage);

        ProblemDetail problemDetail = exceptionHandler.handleInvalidDisputeStatusException(ex);

        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        assertEquals("Invalid Dispute Status", problemDetail.getTitle());
        assertEquals(errorMessage, problemDetail.getDetail());
        assertEquals(URI.create("/errors/dispute-status-conflict"), problemDetail.getType());
    }
}