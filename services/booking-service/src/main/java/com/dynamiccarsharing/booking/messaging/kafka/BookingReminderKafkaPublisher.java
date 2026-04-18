package com.dynamiccarsharing.booking.messaging.kafka;

import com.dynamiccarsharing.contracts.dto.BookingReminderEventDto;
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
public class BookingReminderKafkaPublisher implements BookingReminderPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${application.messaging.topics.booking-reminders:booking.reminders}")
    private String bookingRemindersTopic;

    @Value("${application.messaging.outbox.send-timeout-seconds:10}")
    private int sendTimeoutSeconds;

    @Override
    public void publish(BookingReminderEventDto event) {
        if (event == null || event.getBookingId() == null) {
            return;
        }

        String key = String.valueOf(event.getBookingId());
        String correlationId = UUID.randomUUID().toString();

        try {
            kafkaTemplate.send(bookingRemindersTopic, key, event)
                    .get(sendTimeoutSeconds, TimeUnit.SECONDS);
            if (log.isInfoEnabled()) {
                log.info("Published BookingReminderEventDto bookingId={} type={} (corrId={})",
                        event.getBookingId(), event.getReminderType(), correlationId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Kafka reminder publish interrupted (corrId={})", correlationId, e);
            throw new IllegalStateException("Kafka publish interrupted", e);
        } catch (ExecutionException | TimeoutException e) {
            log.error("Failed to publish BookingReminderEventDto (corrId={})", correlationId, e);
            throw new IllegalStateException("Kafka publish failed", e);
        }
    }
}
