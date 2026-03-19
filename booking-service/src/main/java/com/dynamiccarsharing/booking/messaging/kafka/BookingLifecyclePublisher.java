package com.dynamiccarsharing.booking.messaging.kafka;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;

public interface BookingLifecyclePublisher {
    void publish(BookingLifecycleEventDto event);
}

