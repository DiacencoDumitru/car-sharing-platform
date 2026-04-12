package com.dynamiccarsharing.booking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CarAvailabilityResponseDto {
    boolean available;
    String reason;
}
