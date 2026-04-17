package com.dynamiccarsharing.booking.messaging.kafka;

import com.dynamiccarsharing.contracts.dto.BookingReminderEventDto;

public interface BookingReminderPublisher {
    void publish(BookingReminderEventDto event);
}
