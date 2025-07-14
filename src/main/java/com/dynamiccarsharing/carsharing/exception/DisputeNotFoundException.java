package com.dynamiccarsharing.carsharing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class DisputeNotFoundException extends RuntimeException {

    public DisputeNotFoundException(String message) {
        super(message);
    }

    public DisputeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
