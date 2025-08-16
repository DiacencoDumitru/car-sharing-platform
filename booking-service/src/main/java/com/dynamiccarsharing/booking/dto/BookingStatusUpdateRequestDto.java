package com.dynamiccarsharing.contracts.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;

@Data
public class BookingStatusUpdateRequestDto {
    @NotNull
    private TransactionStatus status;
}