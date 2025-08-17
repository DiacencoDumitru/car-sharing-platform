package com.dynamiccarsharing.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import com.dynamiccarsharing.contracts.enums.PaymentType;

import java.math.BigDecimal;

@Data
public class PaymentRequestDto {
    @NotNull(message = "Booking ID cannot be null.")
    private Long bookingId;

    @NotNull(message = "Amount must not be null.")
    @Positive(message = "Amount must be positive.")
    private BigDecimal amount;

    @NotNull(message = "Payment method must be not null.")
    private PaymentType paymentMethod;
}