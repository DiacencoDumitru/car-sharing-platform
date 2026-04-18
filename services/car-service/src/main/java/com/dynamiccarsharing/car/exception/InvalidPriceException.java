package com.dynamiccarsharing.car.exception;

import com.dynamiccarsharing.util.exception.ConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidPriceException extends ConflictException {
    public InvalidPriceException(String message) {
        super(message);
    }
}