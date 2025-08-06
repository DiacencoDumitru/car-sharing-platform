package com.dynamiccarsharing.carsharing.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RegistrationNumberValidator implements ConstraintValidator<ValidRegistrationNumber, String> {
    private static final String REGISTRATION_PATTERN = "^[A-Z]{3}-\\d{3}$";

    @Override
    public boolean isValid(String registrationNumber, ConstraintValidatorContext context) {
        if (registrationNumber == null) {
            return true;
        }
        return registrationNumber.matches(REGISTRATION_PATTERN);
    }
}