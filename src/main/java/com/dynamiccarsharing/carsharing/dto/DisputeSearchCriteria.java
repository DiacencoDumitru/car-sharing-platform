package com.dynamiccarsharing.carsharing.dto;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DisputeSearchCriteria {
    private Long bookingId;
    private DisputeStatus status;
}