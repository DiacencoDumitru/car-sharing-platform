package com.dynamiccarsharing.carsharing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class InvalidPaymentStatusException extends RuntimeException {
    public InvalidPaymentStatusException(String message) {
        super(message);
    }
}
