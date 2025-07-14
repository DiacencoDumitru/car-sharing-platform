package com.dynamiccarsharing.carsharing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class InvalidVerificationStatusException extends RuntimeException{
    public InvalidVerificationStatusException(String message) {
        super(message);
    }
}
