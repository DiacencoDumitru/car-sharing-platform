package com.dynamiccarsharing.booking.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class CarAvailabilityCalendarResponseDto {
    Long carId;
    List<CarAvailabilityCalendarDayDto> days;
}
