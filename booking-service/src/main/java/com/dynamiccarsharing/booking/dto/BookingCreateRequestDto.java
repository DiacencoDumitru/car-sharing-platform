package com.dynamiccarsharing.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.dynamiccarsharing.booking.validation.StartBeforeEnd;

import java.time.LocalDateTime;

@Data
@StartBeforeEnd
public class BookingCreateRequestDto {
    @NotNull(message = "Renter ID cannot be null.")
    private Long renterId;

    @NotNull(message = "Car ID cannot be null.")
    private Long carId;

    @NotNull(message = "Start time cannot be null.")
    @Future(message = "Start time must be in the future.")
    private LocalDateTime startTime;

    @NotNull(message = "End time cannot be null.")
    @Future(message = "End time must be in the future.")
    private LocalDateTime endTime;

    @NotNull(message = "Pickup location ID cannot be null.")
    private Long pickupLocationId;
}