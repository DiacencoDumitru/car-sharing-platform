package com.dynamiccarsharing.carsharing.exception;

class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}