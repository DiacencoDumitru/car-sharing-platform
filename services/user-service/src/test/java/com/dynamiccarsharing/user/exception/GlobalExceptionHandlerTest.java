package com.dynamiccarsharing.user.exception;

import com.dynamiccarsharing.user.exception.handler.GlobalExceptionHandler;
import com.dynamiccarsharing.util.exception.ConflictException;
import com.dynamiccarsharing.util.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exceptionHandler = new GlobalExceptionHandler();
        when(mockRequest.getRequestURI()).thenReturn("/test-uri");
    }

    @Test
    void handleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Access Denied");

        ProblemDetail problemDetail = exceptionHandler.handleAccessDenied(ex, mockRequest);

        assertEquals(HttpStatus.FORBIDDEN.value(), problemDetail.getStatus());
        assertEquals("Forbidden", problemDetail.getTitle());
        assertEquals("You do not have permission to perform this action.", problemDetail.getDetail());
        assertEquals(URI.create("/errors/forbidden"), problemDetail.getType());
        assertEquals(URI.create("/test-uri"), problemDetail.getInstance());
    }

    @Test
    void handleNoCreds() {
        AuthenticationCredentialsNotFoundException ex = new AuthenticationCredentialsNotFoundException("No creds");

        ProblemDetail problemDetail = exceptionHandler.handleNoCreds(ex, mockRequest);

        assertEquals(HttpStatus.FORBIDDEN.value(), problemDetail.getStatus());
        assertEquals("Forbidden", problemDetail.getTitle());
        assertEquals("Authentication is required for this action.", problemDetail.getDetail());
        assertEquals(URI.create("/errors/forbidden"), problemDetail.getType());
        assertEquals(URI.create("/test-uri"), problemDetail.getInstance());
    }

    @Test
    void handleMethodArgumentNotValidException() throws Exception {
        FieldError fieldError = new FieldError("user", "email", "must be a valid email");
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "user");
        bindingResult.addError(fieldError);

        MethodParameter parameter = new MethodParameter(
                com.dynamiccarsharing.user.controller.UserController.class.getMethod(
                        "registerUser",
                        com.dynamiccarsharing.user.dto.UserCreateRequestDto.class),
                0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ProblemDetail problemDetail = exceptionHandler.handleMethodArgumentNotValidException(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertEquals("Validation Failed", problemDetail.getTitle());
        assertEquals("One or more validation errors occurred.", problemDetail.getDetail());
        assertEquals(URI.create("/errors/validation-failed"), problemDetail.getType());

        Map<String, String> errors = (Map<String, String>) problemDetail.getProperties().get("errors");
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals("must be a valid email", errors.get("email"));
    }

    @Test
    void handleResourceNotFoundException() {
        String errorMessage = "Item 123 not found";
        ResourceNotFoundException ex = new ResourceNotFoundException(errorMessage);

        ProblemDetail problemDetail = exceptionHandler.handleResourceNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("Resource Not Found", problemDetail.getTitle());
        assertEquals(errorMessage, problemDetail.getDetail());
        assertEquals(URI.create("/errors/resource-not-found"), problemDetail.getType());
    }

    @Test
    void handleConflictException() {
        String errorMessage = "Email already in use";
        ConflictException ex = new ConflictException(errorMessage);

        ProblemDetail problemDetail = exceptionHandler.handleConflictException(ex);

        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        assertEquals("Operation Conflict", problemDetail.getTitle());
        assertEquals(errorMessage, problemDetail.getDetail());
        assertEquals(URI.create("/errors/operation-conflict"), problemDetail.getType());
    }

    @Test
    void handleGenericException() {
        Exception ex = new Exception("Something went wrong");

        ProblemDetail problemDetail = exceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
        assertEquals("Internal Server Error", problemDetail.getTitle());
        assertEquals("An unexpected internal error occurred.", problemDetail.getDetail());
        assertEquals(URI.create("/errors/internal-server-error"), problemDetail.getType());
    }

    @Test
    void handleUserNotFoundException() {
        String errorMessage = "User with ID 1 does not exist";
        UserNotFoundException ex = new UserNotFoundException(errorMessage);

        ProblemDetail problemDetail = exceptionHandler.handleUserNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND.value(), problemDetail.getStatus());
        assertEquals("User Not Found", problemDetail.getTitle());
        assertEquals(errorMessage, problemDetail.getDetail());
        assertEquals(URI.create("/errors/user-not-found"), problemDetail.getType());
    }

    @Test
    void handleInvalidUserStatusException() {
        String errorMessage = "Cannot deactivate a suspended user";
        InvalidUserStatusException ex = new InvalidUserStatusException(errorMessage);

        ProblemDetail problemDetail = exceptionHandler.handleInvalidUserStatusException(ex);

        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
        assertEquals("Invalid User Status", problemDetail.getTitle());
        assertEquals(errorMessage, problemDetail.getDetail());
        assertEquals(URI.create("/errors/user-status-conflict"), problemDetail.getType());
    }
}