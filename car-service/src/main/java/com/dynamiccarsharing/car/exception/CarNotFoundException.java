package com.dynamiccarsharing.car.exception;

import com.dynamiccarsharing.util.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)

public class CarNotFoundException extends ResourceNotFoundException {

    public CarNotFoundException(String message) {
        super(message);
    }

    public CarNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}