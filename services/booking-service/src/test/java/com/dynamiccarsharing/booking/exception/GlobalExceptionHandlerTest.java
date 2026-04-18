package com.dynamiccarsharing.booking.exception;

import com.dynamiccarsharing.booking.exception.handler.GlobalExceptionHandler;
import com.dynamiccarsharing.util.exception.ConflictException;
import com.dynamiccarsharing.util.exception.ResourceNotFoundException;
import com.dynamiccarsharing.util.exception.ValidationException;
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
        FieldError fieldError = new FieldError("objectName", "fieldName", "Error message");

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
        assertEquals("Error message", errors.get("fieldName"));
    }

    @Test
    void handleResourceNotFoundException() {
        String errorMessage = "Resource with ID 123 not found";
        ResourceNotFoundException ex = new ResourceNotFoundException(errorMessage);

        ProblemDetail problemDetail = exceptionHandler.handleResourceNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("Resource Not Found", problemDetail.getTitle());
        assertEquals(errorMessage, problemDetail.getDetail());
        assertEquals(URI.create("/errors/resource-not-found"), problemDetail.getType());
    }

    @Test
    void handleConflictException() {
        String errorMessage = "Operation conflict detected";
        ConflictException ex = new ConflictException(errorMessage);

        ProblemDetail problemDetail = exceptionHandler.handleConflictException(ex);

        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        assertEquals("Operation Conflict", problemDetail.getTitle());
        assertEquals(errorMessage, problemDetail.getDetail());
        assertEquals(URI.create("/errors/operation-conflict"), problemDetail.getType());
    }

    @Test
    void handleGenericException() {
        Exception ex = new Exception("An unexpected error");

        ProblemDetail problemDetail = exceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        assertEquals("Internal Server Error", problemDetail.getTitle());
        assertEquals("An unexpected internal error occurred.", problemDetail.getDetail());
        assertEquals(URI.create("/errors/internal-server-error"), problemDetail.getType());
    }

    @Test
    void handleBookingNotFoundException() {
        String errorMessage = "Booking 456 not found";
        BookingNotFoundException ex = new BookingNotFoundException(errorMessage);

        ProblemDetail problemDetail = exceptionHandler.handleBookingNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("Booking Not Found", problemDetail.getTitle());
        assertEquals(errorMessage, problemDetail.getDetail());
        assertEquals(URI.create("/errors/booking-not-found"), problemDetail.getType());
        assertEquals("booking-service", problemDetail.getProperties().get("service"));
    }

    @Test
    void handleInvalidBookingStatusException() {
        String errorMessage = "Cannot cancel a completed booking";
        InvalidBookingStatusException ex = new InvalidBookingStatusException(errorMessage);

        ProblemDetail problemDetail = exceptionHandler.handleInvalidBookingStatusException(ex);

        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        assertEquals("Invalid Booking Status", problemDetail.getTitle());
        assertEquals(errorMessage, problemDetail.getDetail());
        assertEquals(URI.create("/errors/booking-status-conflict"), problemDetail.getType());
    }

    @Test
    void handleValidationException() {
        String errorMessage = "Start date must be before end date";
        ValidationException ex = new ValidationException(errorMessage);

        ProblemDetail problemDetail = exceptionHandler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Validation Failed", problemDetail.getTitle());
        assertEquals(errorMessage, problemDetail.getDetail());
        assertEquals(URI.create("/errors/validation-failed"), problemDetail.getType());
    }
}