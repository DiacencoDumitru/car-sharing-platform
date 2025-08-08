package com.dynamiccarsharing.carsharing.dto;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
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
    private String disputeDescription;
    private DisputeStatus disputeStatus;
}