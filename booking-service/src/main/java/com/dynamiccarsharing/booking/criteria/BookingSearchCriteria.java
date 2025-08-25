package com.dynamiccarsharing.booking.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSearchCriteria {
    private Long renterId;
    private Long carId;

    private TransactionStatus status;

    private LocalDateTime startTimeAfter;
    private LocalDateTime endTimeBefore;
}