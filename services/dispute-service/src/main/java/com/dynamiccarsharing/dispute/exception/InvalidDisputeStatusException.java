package com.dynamiccarsharing.dispute.exception;

import com.dynamiccarsharing.util.exception.ConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class InvalidDisputeStatusException extends ConflictException {
    public InvalidDisputeStatusException(String message) {
        super(message);
    }
}

