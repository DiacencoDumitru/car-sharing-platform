package com.dynamiccarsharing.booking.dto;

import lombok.Data;
import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDto {
    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private TransactionStatus status;
    private PaymentType paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}