package com.dynamiccarsharing.booking.pricing;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PricingContext(
        Long bookingId,
        Long renterId,
        Long carId,
        BigDecimal carPricePerDay,
        Long pickupLocationId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String promoCode
) {
}

