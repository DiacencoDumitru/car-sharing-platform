package com.dynamiccarsharing.booking.dto;

import com.dynamiccarsharing.booking.validation.StartBeforeEnd;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@StartBeforeEnd
public class BookingWaitlistCreateRequestDto {
    @NotNull
    private Long renterId;

    @NotNull
    private Long carId;

    @NotNull
    @Future
    private LocalDateTime startTime;

    @NotNull
    @Future
    private LocalDateTime endTime;

    @NotNull
    private Long pickupLocationId;

    private String promoCode;
}
