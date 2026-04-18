package com.dynamiccarsharing.car.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RegistrationNumberValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRegistrationNumber {
    String message() default "Invalid registration number format.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
