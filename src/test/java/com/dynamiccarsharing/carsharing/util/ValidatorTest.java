package com.dynamiccarsharing.carsharing.util;

import com.dynamiccarsharing.carsharing.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidatorTest {

    @Test
    void validateId_withPositiveId_shouldNotThrowException() {
        assertDoesNotThrow(() -> Validator.validateId(1L, "Test ID"));
    }

    @Test
    void validateId_withNullId_shouldNotThrowException() {
        assertDoesNotThrow(() -> Validator.validateId(null, "Test ID"));
    }

    @Test
    void validateId_withNegativeId_shouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> Validator.validateId(-1L, "Test ID"));
    }

    @Test
    void validateNonEmptyString_withValidString_shouldNotThrowException() {
        assertDoesNotThrow(() -> Validator.validateNonEmptyString("valid", "Field"));
    }

    @Test
    void validateNonEmptyString_withNullString_shouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> Validator.validateNonEmptyString(null, "Field"));
    }

    @Test
    void validateNonEmptyString_withEmptyString_shouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> Validator.validateNonEmptyString(" ", "Field"));
    }

    @Test
    void validateOptionalString_withValidString_shouldNotThrowException() {
        assertDoesNotThrow(() -> Validator.validateOptionalString("valid", "Optional Field"));
    }

    @Test
    void validateOptionalString_withNullString_shouldNotThrowException() {
        assertDoesNotThrow(() -> Validator.validateOptionalString(null, "Optional Field"));
    }

    @Test
    void validateOptionalString_withEmptyString_shouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> Validator.validateOptionalString(" ", "Optional Field"));
    }

    @Test
    void validateNonNegativeDouble_withPositiveValue_shouldNotThrowException() {
        assertDoesNotThrow(() -> Validator.validateNonNegativeDouble(10.5, "Price"));
    }

    @Test
    void validateNonNegativeDouble_withZero_shouldNotThrowException() {
        assertDoesNotThrow(() -> Validator.validateNonNegativeDouble(0.0, "Price"));
    }

    @Test
    void validateNonNegativeDouble_withNegativeValue_shouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> Validator.validateNonNegativeDouble(-5.0, "Price"));
    }

    @Test
    void validateNonNull_withNonNullObject_shouldNotThrowException() {
        assertDoesNotThrow(() -> Validator.validateNonNull(new Object(), "Object"));
    }

    @Test
    void validateNonNull_withNullObject_shouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> Validator.validateNonNull(null, "Object"));
    }

    @Test
    void validateEmail_withValidEmail_shouldNotThrowException() {
        assertDoesNotThrow(() -> Validator.validateEmail("test@example.com", "Email"));
    }

    @Test
    void validateEmail_withNullEmail_shouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> Validator.validateEmail(null, "Email"));
    }

    @Test
    void validateEmail_withInvalidEmail_shouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> Validator.validateEmail("invalid-email", "Email"));
    }

    @Test
    void validateDates_withValidRange_shouldNotThrowException() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        assertDoesNotThrow(() -> Validator.validateDates(start, end, "Start", "End"));
    }

    @Test
    void validateDates_withNullStart_shouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> Validator.validateDates(null, LocalDateTime.now(), "Start", "End"));
    }

    @Test
    void validateDates_withStartAfterEnd_shouldThrowValidationException() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusDays(1);
        assertThrows(ValidationException.class, () -> Validator.validateDates(start, end, "Start", "End"));
    }

    @Test
    void validateNonNullList_withValidList_shouldNotThrowException() {
        assertDoesNotThrow(() -> Validator.validateNonNullList(Collections.emptyList(), "List"));
    }

    @Test
    void validateNonNullList_withNullList_shouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> Validator.validateNonNullList(null, "List"));
    }
}