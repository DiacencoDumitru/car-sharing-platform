package com.dynamiccarsharing.carsharing.dto.criteria;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import lombok.*;

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