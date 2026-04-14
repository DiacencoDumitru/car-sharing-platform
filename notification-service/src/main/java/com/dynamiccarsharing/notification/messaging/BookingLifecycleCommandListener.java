package com.dynamiccarsharing.notification.messaging;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import com.dynamiccarsharing.notification.application.BookingLifecycleEventProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "application.messaging.kafka.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class BookingLifecycleCommandListener {

    private final BookingLifecycleEventProcessor processor;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${application.messaging.topics.booking-commands:booking.commands}",
            containerFactory = "bookingLifecycleKafkaListenerContainerFactory",
            concurrency = "2",
            autoStartup = "${application.messaging.kafka.enabled:false}"
    )
    public void onBookingLifecycleEvent(String payload) {
        if (payload == null || payload.isBlank()) {
            return;
        }
        try {
            BookingLifecycleEventDto event = objectMapper.readValue(payload, BookingLifecycleEventDto.class);
            processor.process(event);
        } catch (Exception ex) {
        }
    }
}

