package com.dynamiccarsharing.car.exception;

import com.dynamiccarsharing.car.exception.handler.GlobalExceptionHandler;
import com.dynamiccarsharing.util.exception.ConflictException;
import com.dynamiccarsharing.util.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        FieldError fieldError = new FieldError("car", "model", "Model cannot be null");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ProblemDetail problemDetail = exceptionHandler.handleMethodArgumentNotValidException(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Validation Failed", problemDetail.getTitle());
        assertEquals("One or more validation errors occurred.", problemDetail.getDetail());
        assertEquals(URI.create("/errors/validation-failed"), problemDetail.getType());

        Map<String, String> errors = (Map<String, String>) problemDetail.getProperties().get("errors");
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals("Model cannot be null", errors.get("model"));
    }

    @Test
    void handleResourceNotFoundException() {
        String errorMessage = "Car with ID 123 not found";
        ResourceNotFoundException ex = new ResourceNotFoundException(errorMessage);

        ProblemDetail problemDetail = exceptionHandler.handleResourceNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("Resource Not Found", problemDetail.getTitle());
        assertEquals(errorMessage, problemDetail.getDetail());
        assertEquals(URI.create("/errors/resource-not-found"), problemDetail.getType());
    }

    @Test
    void handleConflictException() {
        String errorMessage = "Car is already booked for this time";
        ConflictException ex = new ConflictException(errorMessage);

        ProblemDetail problemDetail = exceptionHandler.handleConflictException(ex);

        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        assertEquals("Operation Conflict", problemDetail.getTitle());
        assertEquals(errorMessage, problemDetail.getDetail());
        assertEquals(URI.create("/errors/operation-conflict"), problemDetail.getType());
    }

    @Test
    void handleAccessDenied() {
        String errorMessage = "User is not authorized to perform this action";
        AccessDeniedException ex = new AccessDeniedException(errorMessage);

        ProblemDetail problemDetail = exceptionHandler.handleAccessDenied(ex);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), problemDetail.getStatus());
        assertEquals("Unauthorized", problemDetail.getTitle());
        assertEquals(errorMessage, problemDetail.getDetail());
        assertEquals(URI.create("/errors/unauthorized"), problemDetail.getType());
    }

    @Test
    void handleNumberFormat() {
        NumberFormatException ex = new NumberFormatException("For input string: \"abc\"");

        ProblemDetail problemDetail = exceptionHandler.handleNumberFormat(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Invalid Input", problemDetail.getTitle());
        assertEquals("Invalid user id.", problemDetail.getDetail());
        assertEquals(URI.create("/errors/invalid-input"), problemDetail.getType());
    }

    @Test
    void handleDataIntegrity_DataIntegrityViolationException() {
        String errorMessage = "Duplicate key value violates unique constraint";
        DataIntegrityViolationException ex = new DataIntegrityViolationException(errorMessage);

        ProblemDetail problemDetail = exceptionHandler.handleDataIntegrity(ex);

        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        assertEquals("Constraint Violation", problemDetail.getTitle());
        assertTrue(problemDetail.getDetail().contains("Data integrity violation."));
        assertTrue(problemDetail.getDetail().contains(errorMessage));
        assertEquals(URI.create("/errors/constraint-violation"), problemDetail.getType());
    }

    @Test
    void handleDataIntegrity_ConstraintViolationException() {
        String errorMessage = "Validation failed for ...";
        ConstraintViolationException ex = new ConstraintViolationException(errorMessage, Collections.emptySet());

        ProblemDetail problemDetail = exceptionHandler.handleDataIntegrity(ex);

        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        assertEquals("Constraint Violation", problemDetail.getTitle());
        assertTrue(problemDetail.getDetail().contains("Data integrity violation."));
        assertTrue(problemDetail.getDetail().contains(errorMessage));
        assertEquals(URI.create("/errors/constraint-violation"), problemDetail.getType());
    }
    
    @Test
    void handleDataIntegrity_NullMessage() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(null);

        ProblemDetail problemDetail = exceptionHandler.handleDataIntegrity(ex);

        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        assertEquals("Constraint Violation", problemDetail.getTitle());
        assertEquals("Data integrity violation. ", problemDetail.getDetail());
        assertEquals(URI.create("/errors/constraint-violation"), problemDetail.getType());
    }

    @Test
    void handleInvalidCarStatusException() {
        String errorMessage = "Car must be AVAILABLE to be booked";
        InvalidCarStatusException ex = new InvalidCarStatusException(errorMessage);

        ProblemDetail problemDetail = exceptionHandler.handleInvalidCarStatusException(ex);

        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        assertEquals("Invalid Car Status", problemDetail.getTitle());
        assertEquals(errorMessage, problemDetail.getDetail());
        assertEquals(URI.create("/errors/car-status-conflict"), problemDetail.getType());
    }

    @Test
    void handleInvalidPriceException() {
        String errorMessage = "Price cannot be negative";
        InvalidPriceException ex = new InvalidPriceException(errorMessage);

        ProblemDetail problemDetail = exceptionHandler.handleInvalidPriceException(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Invalid Price", problemDetail.getTitle());
        assertEquals(errorMessage, problemDetail.getDetail());
        assertEquals(URI.create("/errors/invalid-price"), problemDetail.getType());
    }

    @Test
    void handleGenericException() {
        Exception ex = new Exception("A generic runtime error");

        ProblemDetail problemDetail = exceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        assertEquals("Internal Server Error", problemDetail.getTitle());
        assertEquals("An unexpected internal error occurred.", problemDetail.getDetail());
        assertEquals(URI.create("/errors/internal-server-error"), problemDetail.getType());
    }
}