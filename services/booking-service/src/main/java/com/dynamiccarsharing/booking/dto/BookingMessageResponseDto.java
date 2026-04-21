package com.dynamiccarsharing.booking.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class BookingMessageResponseDto {
    Long id;
    Long senderUserId;
    String body;
    LocalDateTime createdAt;
}
