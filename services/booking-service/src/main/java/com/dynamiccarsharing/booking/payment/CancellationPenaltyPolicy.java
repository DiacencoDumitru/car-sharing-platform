package com.dynamiccarsharing.booking.payment;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class CancellationPenaltyPolicy {

    private static final BigDecimal FULL_REFUND = new BigDecimal("1.00");
    private static final BigDecimal HALF_REFUND = new BigDecimal("0.50");
    private static final BigDecimal NO_REFUND = BigDecimal.ZERO;

    public BigDecimal resolveRefundRatio(LocalDateTime startTime, LocalDateTime canceledAt) {
        long hoursBeforeStart = Duration.between(canceledAt, startTime).toHours();
        if (hoursBeforeStart >= 24) {
            return FULL_REFUND;
        }
        if (hoursBeforeStart >= 1) {
            return HALF_REFUND;
        }
        return NO_REFUND;
    }
}
