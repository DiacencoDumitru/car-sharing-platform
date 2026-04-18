package com.dynamiccarsharing.contracts.dto;

import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingLifecycleEventDto {
    private Long bookingId;
    private Long renterId;
    private Long carId;
    private TransactionStatus bookingStatus;
    private Instant occurredAt;
}

