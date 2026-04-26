package com.dynamiccarsharing.dispute.exception.handler;

import com.dynamiccarsharing.dispute.exception.DisputeNotFoundException;
import com.dynamiccarsharing.dispute.exception.InvalidDisputeStatusException;
import com.dynamiccarsharing.util.web.AbstractGlobalExceptionHandler;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    @Override
    protected String serviceName() {
        return "dispute-service";
    }

    @ExceptionHandler(DisputeNotFoundException.class)
    public ProblemDetail handleDisputeNotFoundException(DisputeNotFoundException ex) {
        return problem(NOT_FOUND, "Dispute Not Found", ex.getMessage(), "/errors/dispute-not-found");
    }

    @ExceptionHandler(InvalidDisputeStatusException.class)
    public ProblemDetail handleInvalidDisputeStatusException(InvalidDisputeStatusException ex) {
        return problem(CONFLICT, "Invalid Dispute Status", ex.getMessage(), "/errors/dispute-status-conflict");
    }

}