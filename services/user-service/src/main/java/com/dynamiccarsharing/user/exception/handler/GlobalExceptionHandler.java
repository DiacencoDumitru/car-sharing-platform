package com.dynamiccarsharing.user.exception.handler;

import com.dynamiccarsharing.user.exception.InvalidUserStatusException;
import com.dynamiccarsharing.user.exception.UserNotFoundException;
import com.dynamiccarsharing.util.web.AbstractGlobalExceptionHandler;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFoundException(UserNotFoundException ex) {
        return problem(NOT_FOUND, "User Not Found", ex.getMessage(), "/errors/user-not-found");
    }

    @ExceptionHandler(InvalidUserStatusException.class)
    public ProblemDetail handleInvalidUserStatusException(InvalidUserStatusException ex) {
        return problem(CONFLICT, "Invalid User Status", ex.getMessage(), "/errors/user-status-conflict");
    }
}