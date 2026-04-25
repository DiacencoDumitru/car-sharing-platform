package com.dynamiccarsharing.user.exception.handler;

import com.dynamiccarsharing.user.exception.InvalidUserStatusException;
import com.dynamiccarsharing.user.exception.UserNotFoundException;
import com.dynamiccarsharing.util.web.AbstractGlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return super.handleAccessDenied(ex);
    }

    public ProblemDetail handleNoCreds(AuthenticationCredentialsNotFoundException ex, HttpServletRequest req) {
        return super.handleAccessDenied(ex);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFoundException(UserNotFoundException ex) {
        return problem(NOT_FOUND, "User Not Found", ex.getMessage(), "/errors/user-not-found");
    }

    @ExceptionHandler(InvalidUserStatusException.class)
    public ProblemDetail handleInvalidUserStatusException(InvalidUserStatusException ex) {
        return problem(CONFLICT, "Invalid User Status", ex.getMessage(), "/errors/user-status-conflict");
    }
}