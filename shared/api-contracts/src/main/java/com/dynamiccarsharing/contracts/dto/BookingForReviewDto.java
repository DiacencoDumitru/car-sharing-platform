package com.dynamiccarsharing.contracts.dto;

import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingForReviewDto {
    private Long bookingId;
    private Long renterId;
    private Long carId;
    private TransactionStatus status;
}
