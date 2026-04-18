package com.dynamiccarsharing.contracts.dto;

import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingDto {
    private Long id;
    private Long renterId;
    private Long carId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private TransactionStatus status;
    private Long pickupLocationId;
    private String instanceId;
}