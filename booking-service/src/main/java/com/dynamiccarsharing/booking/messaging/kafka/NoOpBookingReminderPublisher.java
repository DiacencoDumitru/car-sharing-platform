package com.dynamiccarsharing.booking.messaging.kafka;

import com.dynamiccarsharing.contracts.dto.BookingReminderEventDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "application.messaging.kafka.enabled",
        havingValue = "false",
        matchIfMissing = true
)
public class NoOpBookingReminderPublisher implements BookingReminderPublisher {

    @Override
    public void publish(BookingReminderEventDto event) {
    }
}
