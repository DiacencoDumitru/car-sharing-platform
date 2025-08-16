package com.dynamiccarsharing.car.exception;

import com.dynamiccarsharing.util.exception.ConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class InvalidVerificationStatusException extends ConflictException {
    public InvalidVerificationStatusException(String message) {
        super(message);
    }
}
