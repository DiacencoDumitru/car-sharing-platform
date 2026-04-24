package com.dynamiccarsharing.booking.dto;

import com.dynamiccarsharing.booking.model.BookingWaitlistStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class BookingWaitlistResponseDto {
    Long id;
    Long renterId;
    Long carId;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Long pickupLocationId;
    String promoCode;
    BookingWaitlistStatus status;
    LocalDateTime createdAt;
}
