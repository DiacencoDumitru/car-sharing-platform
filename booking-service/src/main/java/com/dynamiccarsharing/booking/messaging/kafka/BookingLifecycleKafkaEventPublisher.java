package com.dynamiccarsharing.booking.messaging.kafka;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@ConditionalOnProperty(
        name = "application.messaging.kafka.enabled",
        havingValue = "true",
        matchIfMissing = false
)
@RequiredArgsConstructor
@Slf4j
public class BookingLifecycleKafkaEventPublisher implements BookingLifecyclePublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${application.messaging.topics.booking-commands:booking.commands}")
    private String bookingCommandsTopic;

    @Value("${application.messaging.outbox.send-timeout-seconds:10}")
    private int sendTimeoutSeconds;

    @Override
    public void publish(BookingLifecycleEventDto event) {
        if (event == null) {
            return;
        }

        String key = event.getBookingId() != null ? String.valueOf(event.getBookingId()) : "booking";
        String correlationId = UUID.randomUUID().toString();

        try {
            var result = kafkaTemplate.send(bookingCommandsTopic, key, event)
                    .get(sendTimeoutSeconds, TimeUnit.SECONDS);
            if (log.isInfoEnabled() && result != null) {
                var recordMetadata = result.getRecordMetadata();
                log.info("Published BookingLifecycleEventDto {}@{}:{} (corrId={})",
                        event.getBookingStatus(), recordMetadata.partition(), recordMetadata.offset(), correlationId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Kafka publish interrupted for topic {} (corrId={})", bookingCommandsTopic, correlationId, e);
            throw new IllegalStateException("Kafka publish interrupted", e);
        } catch (ExecutionException | TimeoutException e) {
            log.error("Failed to publish BookingLifecycleEventDto to {} (corrId={})",
                    bookingCommandsTopic, correlationId, e);
            throw new IllegalStateException("Kafka publish failed", e);
        }
    }
}
