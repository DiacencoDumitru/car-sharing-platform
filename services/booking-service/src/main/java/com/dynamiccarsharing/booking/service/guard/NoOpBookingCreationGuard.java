package com.dynamiccarsharing.booking.service.guard;

import com.dynamiccarsharing.booking.service.interfaces.BookingCreationGuard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@ConditionalOnProperty(
        name = "application.redis.booking-guard.enabled",
        havingValue = "false",
        matchIfMissing = true
)
@Slf4j
public class NoOpBookingCreationGuard implements BookingCreationGuard {
    @Override
    public <T> T executeWithCarLock(Long carId, Supplier<T> action) {
        if (log.isDebugEnabled()) {
            log.debug("Booking creation guard is disabled for carId={}", carId);
        }
        return action.get();
    }
}
