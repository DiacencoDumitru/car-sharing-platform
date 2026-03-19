package com.dynamiccarsharing.booking.messaging.kafka;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "application.messaging.kafka.enabled",
        havingValue = "false",
        matchIfMissing = true
)
@Slf4j
public class NoOpBookingLifecyclePublisher implements BookingLifecyclePublisher {

    @Override
    public void publish(BookingLifecycleEventDto event) {
        if (log.isDebugEnabled() && event != null) {
            log.debug("Skipping Kafka booking lifecycle publish bookingId={}, status={}",
                    event.getBookingId(), event.getBookingStatus());
        }
    }
}

