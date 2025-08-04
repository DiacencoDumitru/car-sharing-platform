package com.dynamiccarsharing.carsharing.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

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
    void validateId_withNegativeId_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Validator.validateId(-1L, "Test ID"));
    }

    @Test
    void validateNonEmptyString_withValidString_shouldNotThrowException() {
        assertDoesNotThrow(() -> Validator.validateNonEmptyString("valid", "Field"));
    }

    @Test
    void validateNonEmptyString_withNullString_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Validator.validateNonEmptyString(null, "Field"));
    }

    @Test
    void validateNonEmptyString_withEmptyString_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Validator.validateNonEmptyString(" ", "Field"));
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
    void validateOptionalString_withEmptyString_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Validator.validateOptionalString(" ", "Optional Field"));
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
    void validateNonNegativeDouble_withNegativeValue_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Validator.validateNonNegativeDouble(-5.0, "Price"));
    }

    @Test
    void validateNonNull_withNonNullObject_shouldNotThrowException() {
        assertDoesNotThrow(() -> Validator.validateNonNull(new Object(), "Object"));
    }

    @Test
    void validateNonNull_withNullObject_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Validator.validateNonNull(null, "Object"));
    }

    @Test
    void validateEmail_withValidEmail_shouldNotThrowException() {
        assertDoesNotThrow(() -> Validator.validateEmail("test@example.com", "Email"));
    }

    @Test
    void validateEmail_withNullEmail_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Validator.validateEmail(null, "Email"));
    }

    @Test
    void validateEmail_withInvalidEmail_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Validator.validateEmail("invalid-email", "Email"));
    }

    @Test
    void validateDates_withValidRange_shouldNotThrowException() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        assertDoesNotThrow(() -> Validator.validateDates(start, end, "Start", "End"));
    }

    @Test
    void validateDates_withNullStart_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Validator.validateDates(null, LocalDateTime.now(), "Start", "End"));
    }

    @Test
    void validateDates_withStartAfterEnd_shouldThrowIllegalArgumentException() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusDays(1);
        assertThrows(IllegalArgumentException.class, () -> Validator.validateDates(start, end, "Start", "End"));
    }

    @Test
    void validateNonNullList_withValidList_shouldNotThrowException() {
        assertDoesNotThrow(() -> Validator.validateNonNullList(Collections.emptyList(), "List"));
    }

    @Test
    void validateNonNullList_withNullList_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> Validator.validateNonNullList(null, "List"));
    }
}