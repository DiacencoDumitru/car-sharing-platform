package com.dynamiccarsharing.carsharing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class InvalidDisputeStatusException extends ConflictException {
    public InvalidDisputeStatusException(String message) {
        super(message);
    }
}

