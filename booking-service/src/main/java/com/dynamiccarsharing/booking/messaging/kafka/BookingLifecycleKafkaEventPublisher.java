package com.dynamiccarsharing.booking.messaging.kafka;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

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

    @Override
    public void publish(BookingLifecycleEventDto event) {
        if (event == null) {
            return;
        }

        String key = event.getBookingId() != null ? String.valueOf(event.getBookingId()) : "booking";
        String correlationId = UUID.randomUUID().toString();

        kafkaTemplate.send(bookingCommandsTopic, key, event)
                .whenComplete((md, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish BookingLifecycleEventDto to {} (corrId={})",
                                bookingCommandsTopic, correlationId, ex);
                    } else if (md != null) {
                        var recordMetadata = md.getRecordMetadata();
                        log.info("Published BookingLifecycleEventDto {}@{}:{} (corrId={})",
                                event.getBookingStatus(), recordMetadata.partition(), recordMetadata.offset(), correlationId);
                    }
                });
    }
}

