package com.dynamiccarsharing.carsharing.exception;

/**
 * Custom exception for failures in the data access layer (DAO/Repository).
 * This wraps lower-level exceptions like SQLException.
 */
public class RepositoryException extends RuntimeException {

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}