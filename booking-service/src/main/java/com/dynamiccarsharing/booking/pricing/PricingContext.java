package com.dynamiccarsharing.booking.pricing;

import java.time.LocalDateTime;

public record PricingContext(
        Long bookingId,
        Long renterId,
        Long carId,
        Long pickupLocationId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String promoCode
) {
}

