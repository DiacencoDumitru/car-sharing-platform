package com.dynamiccarsharing.booking.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class QuoteResponseDto {
    Long renterId;
    Long carId;
    BigDecimal baseAmount;
    BigDecimal dynamicMarkupAmount;
    BigDecimal discountAmount;
    BigDecimal loyaltyAmount;
    BigDecimal totalAmount;
    String currency;
    LocalDateTime expiresAt;
}
