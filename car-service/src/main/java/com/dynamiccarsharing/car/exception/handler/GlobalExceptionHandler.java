package com.dynamiccarsharing.car.exception.handler;

import com.dynamiccarsharing.car.exception.InvalidCarStatusException;
import com.dynamiccarsharing.car.exception.InvalidPriceException;
import com.dynamiccarsharing.util.exception.ConflictException;
import com.dynamiccarsharing.util.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "One or more validation errors occurred.");
        pd.setTitle("Validation Failed");
        pd.setType(URI.create("/errors/validation-failed"));

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
        pd.setProperty("errors", fieldErrors);
        return pd;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Resource Not Found");
        pd.setType(URI.create("/errors/resource-not-found"));
        return pd;
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflictException(ConflictException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Operation Conflict");
        pd.setType(URI.create("/errors/operation-conflict"));
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        pd.setTitle("Unauthorized");
        pd.setType(URI.create("/errors/unauthorized"));
        return pd;
    }

    @ExceptionHandler(NumberFormatException.class)
    public ProblemDetail handleNumberFormat(NumberFormatException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid user id.");
        pd.setTitle("Invalid Input");
        pd.setType(URI.create("/errors/invalid-input"));
        return pd;
    }

    @ExceptionHandler({
            DataIntegrityViolationException.class,
            ConstraintViolationException.class
    })
    public ProblemDetail handleDataIntegrity(RuntimeException ex) {
        String detail = ex.getMessage();
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Data integrity violation. " + (detail != null ? detail : ""));
        pd.setTitle("Constraint Violation");
        pd.setType(URI.create("/errors/constraint-violation"));
        return pd;
    }

    @ExceptionHandler(InvalidCarStatusException.class)
    public ProblemDetail handleInvalidCarStatusException(InvalidCarStatusException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Invalid Car Status");
        pd.setType(URI.create("/errors/car-status-conflict"));
        return pd;
    }

    @ExceptionHandler(InvalidPriceException.class)
    public ProblemDetail handleInvalidPriceException(InvalidPriceException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Invalid Price");
        pd.setType(URI.create("/errors/invalid-price"));
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal error occurred.");
        pd.setTitle("Internal Server Error");
        pd.setType(URI.create("/errors/internal-server-error"));
        return pd;
    }
}