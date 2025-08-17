package com.dynamiccarsharing.booking.validation;

import com.dynamiccarsharing.booking.dto.BookingCreateRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class StartBeforeEndValidator implements ConstraintValidator<StartBeforeEnd, BookingCreateRequestDto> {

    @Override
    public boolean isValid(BookingCreateRequestDto dto, ConstraintValidatorContext context) {
        LocalDateTime startTime = dto.getStartTime();
        LocalDateTime endTime = dto.getEndTime();

        if (startTime == null || endTime == null) {
            return true;
        }

        return startTime.isBefore(endTime);
    }
}