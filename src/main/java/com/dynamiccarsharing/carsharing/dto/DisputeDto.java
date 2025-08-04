package com.dynamiccarsharing.carsharing.dto;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DisputeDto {
    private Long id;
    private Long bookingId;
    private Long creationUserId;
    private String description;
    private DisputeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}