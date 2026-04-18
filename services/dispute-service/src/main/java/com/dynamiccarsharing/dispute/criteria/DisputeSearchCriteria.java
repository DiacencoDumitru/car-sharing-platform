package com.dynamiccarsharing.dispute.criteria;

import lombok.Builder;
import lombok.Getter;
import com.dynamiccarsharing.contracts.enums.DisputeStatus;

@Getter
@Builder
public class DisputeSearchCriteria {
    private Long bookingId;
    private DisputeStatus status;
}