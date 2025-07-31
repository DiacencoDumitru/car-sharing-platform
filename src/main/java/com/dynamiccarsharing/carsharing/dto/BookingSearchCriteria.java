package com.dynamiccarsharing.carsharing.dto;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookingSearchCriteria {
    private Long renterId;
    private Long carId;
    private TransactionStatus status;
}