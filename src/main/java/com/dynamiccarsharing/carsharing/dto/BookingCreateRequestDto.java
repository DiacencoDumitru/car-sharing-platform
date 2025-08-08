package com.dynamiccarsharing.carsharing.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingCreateRequestDto {
    @NotNull
    private Long renterId;
    @NotNull
    private Long carId;
    @NotNull @Future
    private LocalDateTime startTime;
    @NotNull @Future
    private LocalDateTime endTime;
    @NotNull
    private Long pickupLocationId;
}