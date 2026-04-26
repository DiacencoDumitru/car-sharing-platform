package com.dynamiccarsharing.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.dynamiccarsharing.contracts.enums.PaymentType;

import java.math.BigDecimal;

@Data
public class PaymentRequestDto {
    @Deprecated(forRemoval = false)
    private Long bookingId;

    @Deprecated(forRemoval = false)
    private BigDecimal amount;

    @NotNull(message = "Payment method must be not null.")
    private PaymentType paymentMethod;

    private BigDecimal loyaltyPointsToUse;
}