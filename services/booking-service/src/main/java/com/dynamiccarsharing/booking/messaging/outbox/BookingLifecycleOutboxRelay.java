package com.dynamiccarsharing.booking.messaging.outbox;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "application.messaging.kafka.enabled", havingValue = "true")
@SuppressFBWarnings(value = "URF_UNREAD_FIELD", justification = "processor is used from @Scheduled drainOutbox; SpotBugs misses scheduled entrypoints")
public class BookingLifecycleOutboxRelay {

    private final BookingLifecycleOutboxRelayProcessor processor;

    public BookingLifecycleOutboxRelay(BookingLifecycleOutboxRelayProcessor processor) {
        this.processor = processor;
    }

    @Value("${application.messaging.outbox.batch-size:50}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${application.messaging.outbox.relay-interval-ms:1000}")
    public void drainOutbox() {
        for (int i = 0; i < batchSize; i++) {
            processor.processOne();
        }
    }
}
