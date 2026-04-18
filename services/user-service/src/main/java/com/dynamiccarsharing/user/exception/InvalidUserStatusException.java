package com.dynamiccarsharing.user.exception;

import com.dynamiccarsharing.util.exception.ConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class InvalidUserStatusException extends ConflictException {
    public InvalidUserStatusException(String message) {
        super(message);
    }
}