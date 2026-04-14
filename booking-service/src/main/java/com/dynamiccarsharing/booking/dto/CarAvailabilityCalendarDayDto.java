package com.dynamiccarsharing.booking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CarAvailabilityCalendarDayDto {
    LocalDate date;
    boolean available;
    String reason;
}
