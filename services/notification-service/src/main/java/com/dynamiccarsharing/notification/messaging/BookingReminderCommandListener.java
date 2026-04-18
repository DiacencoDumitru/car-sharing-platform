package com.dynamiccarsharing.notification.messaging;

import com.dynamiccarsharing.contracts.dto.BookingReminderEventDto;
import com.dynamiccarsharing.notification.application.BookingReminderEventProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "application.messaging.kafka.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class BookingReminderCommandListener {

    private final BookingReminderEventProcessor processor;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${application.messaging.topics.booking-reminders:booking.reminders}",
            containerFactory = "bookingLifecycleKafkaListenerContainerFactory",
            concurrency = "1",
            autoStartup = "${application.messaging.kafka.enabled:false}"
    )
    public void onBookingReminderEvent(String payload) {
        if (payload == null || payload.isBlank()) {
            return;
        }
        try {
            BookingReminderEventDto event = objectMapper.readValue(payload, BookingReminderEventDto.class);
            processor.process(event);
        } catch (Exception ex) {
            log.warn("Failed to process booking reminder payload: {}", ex.toString());
        }
    }
}
