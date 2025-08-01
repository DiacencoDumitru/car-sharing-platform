package com.dynamiccarsharing.carsharing.dto;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequestDto {
    @NotNull(message = "Amount must not be null.")
    @Positive(message = "Amount must be positive.")
    private BigDecimal amount;

    @NotNull(message = "Payment method must be not null.")
    private PaymentType paymentMethod;
}