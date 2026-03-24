package com.dynamiccarsharing.booking.messaging.outbox;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;

public interface BookingLifecycleOutboxWriter {

    void enqueueIfKafkaEnabled(BookingLifecycleEventDto event);
}
