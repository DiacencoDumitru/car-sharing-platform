package com.dynamiccarsharing.carsharing.dto.criteria;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BookingSearchCriteria {
    private Long renterId;
    private Long carId;

    private TransactionStatus status;

    private LocalDateTime startTimeAfter;
    private LocalDateTime endTimeBefore;
}