package com.dynamiccarsharing.car.exception.handler;

import com.dynamiccarsharing.car.exception.InvalidCarStatusException;
import com.dynamiccarsharing.car.exception.InvalidPriceException;
import com.dynamiccarsharing.util.web.AbstractGlobalExceptionHandler;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    @Override
    protected String serviceName() {
        return "car-service";
    }

    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return super.handleAccessDenied(ex);
    }

    @ExceptionHandler(NumberFormatException.class)
    public ProblemDetail handleNumberFormat(NumberFormatException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid Input", "Invalid user id.", "/errors/invalid-input");
    }

    @ExceptionHandler({
            DataIntegrityViolationException.class,
            ConstraintViolationException.class
    })
    public ProblemDetail handleDataIntegrity(RuntimeException ex) {
        String detail = ex.getMessage();
        return problem(HttpStatus.CONFLICT, "Constraint Violation",
                "Data integrity violation. " + (detail != null ? detail : ""), "/errors/constraint-violation");
    }

    @ExceptionHandler(InvalidCarStatusException.class)
    public ProblemDetail handleInvalidCarStatusException(InvalidCarStatusException ex) {
        return problem(HttpStatus.CONFLICT, "Invalid Car Status", ex.getMessage(), "/errors/car-status-conflict");
    }

    @ExceptionHandler({
            ObjectOptimisticLockingFailureException.class,
            OptimisticLockException.class
    })
    public ProblemDetail handleOptimisticLockException(RuntimeException ex) {
        return problem(HttpStatus.CONFLICT, "Concurrent Update Conflict",
                "Car was modified by another request. Please retry.", "/errors/concurrent-update-conflict");
    }

    @ExceptionHandler(InvalidPriceException.class)
    public ProblemDetail handleInvalidPriceException(InvalidPriceException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid Price", ex.getMessage(), "/errors/invalid-price");
    }
}