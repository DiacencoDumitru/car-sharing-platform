package com.dynamiccarsharing.booking.exception;

import com.dynamiccarsharing.util.exception.ConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class InvalidPaymentStatusException extends ConflictException {
    public InvalidPaymentStatusException(String message) {
        super(message);
    }
}
