package com.dynamiccarsharing.booking.messaging.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "application.messaging.kafka.enabled", havingValue = "true")
@RequiredArgsConstructor
public class BookingLifecycleOutboxRelay {

    private final BookingLifecycleOutboxRelayProcessor processor;

    @Value("${application.messaging.outbox.batch-size:50}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${application.messaging.outbox.relay-interval-ms:1000}")
    public void drainOutbox() {
        for (int i = 0; i < batchSize; i++) {
            processor.processOne();
        }
    }
}
