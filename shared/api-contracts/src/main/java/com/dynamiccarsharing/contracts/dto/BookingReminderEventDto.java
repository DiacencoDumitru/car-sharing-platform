package com.dynamiccarsharing.contracts.dto;

import com.dynamiccarsharing.contracts.enums.BookingReminderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingReminderEventDto {
    private Long bookingId;
    private Long renterId;
    private Long carId;
    private BookingReminderType reminderType;
    private Instant occurredAt;
}
