package com.dynamiccarsharing.booking.criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSearchCriteria {
    private Long renterId;
    private Long carId;
    private List<Long> carIds;

    private TransactionStatus status;

    private LocalDateTime startTimeAfter;
    private LocalDateTime endTimeBefore;
}