package com.dynamiccarsharing.carsharing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class CarReviewNotFoundException extends RuntimeException {

    public CarReviewNotFoundException(String message) {
        super(message);
    }

    public CarReviewNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}