package com.dynamiccarsharing.carsharing.dto;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDto {
    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private TransactionStatus status;
    private PaymentType paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}