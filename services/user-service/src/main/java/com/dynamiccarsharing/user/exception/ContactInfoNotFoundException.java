package com.dynamiccarsharing.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ContactInfoNotFoundException extends RuntimeException {

    public ContactInfoNotFoundException(String message) {
        super(message);
    }

    public ContactInfoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}


