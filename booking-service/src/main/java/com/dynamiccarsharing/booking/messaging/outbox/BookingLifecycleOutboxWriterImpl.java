package com.dynamiccarsharing.booking.messaging.outbox;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class BookingLifecycleOutboxWriterImpl implements BookingLifecycleOutboxWriter {

    private final BookingLifecycleOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Value("${application.messaging.kafka.enabled:false}")
    private boolean kafkaEnabled;

    @Override
    public void enqueueIfKafkaEnabled(BookingLifecycleEventDto event) {
        if (!kafkaEnabled || event == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(event);
            outboxRepository.save(BookingLifecycleOutbox.builder()
                    .payload(json)
                    .createdAt(Instant.now())
                    .build());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize booking lifecycle event for outbox", e);
        }
    }
}
