package com.dynamiccarsharing.user.dto.me;

import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionItemDto {
    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private TransactionStatus status;
    private PaymentType paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
