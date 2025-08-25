package com.dynamiccarsharing.util.util;

import com.dynamiccarsharing.util.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

public class Validator {

    private Validator() {
    }

    public static void validateId(Long id, String fieldName) {
        if (id != null && id < 0) {
            throw new ValidationException(fieldName + " must be non-negative");
        }
    }

    public static void validateNonEmptyString(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " must be non-null and non-empty");
        }
    }

    public static void validateOptionalString(String value, String fieldName) {
        if (value != null && value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " must be non-empty if provided");
        }
    }

    public static void validateNonNegativeDouble(double value, String fieldName) {
        if (value < 0) {
            throw new ValidationException(fieldName + " must be non-negative");
        }
    }

    public static void validateNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " must be non-null");
        }
    }

    public static void validateEmail(String email, String fieldName) {
        if (email == null || email.trim().isEmpty() || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new ValidationException(fieldName + " must be non-null, non-empty, and a valid email address");
        }
    }

    public static void validateDates(LocalDateTime start, LocalDateTime end, String startFieldName, String endFieldName) {
        if (start == null || end == null) {
            throw new ValidationException(startFieldName + " and " + endFieldName + " must be non-null");
        }
        if (start.isAfter(end)) {
            throw new ValidationException(startFieldName + " must be before " + endFieldName);
        }
    }

    public static void validateNonNullList(List<?> list, String fieldName) {
        if (list == null) {
            throw new ValidationException(fieldName + " must be non-null");
        }
    }
}