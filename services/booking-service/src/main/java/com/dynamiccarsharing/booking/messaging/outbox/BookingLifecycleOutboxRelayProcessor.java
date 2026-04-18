package com.dynamiccarsharing.booking.messaging.outbox;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import com.dynamiccarsharing.booking.messaging.kafka.BookingLifecyclePublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@ConditionalOnProperty(name = "application.messaging.kafka.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class BookingLifecycleOutboxRelayProcessor {

    private final BookingLifecycleOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final BookingLifecyclePublisher bookingLifecyclePublisher;

    @Transactional
    public void processOne() {
        List<BookingLifecycleOutbox> batch = outboxRepository.lockBatch(1);
        if (batch.isEmpty()) {
            return;
        }
        BookingLifecycleOutbox row = batch.get(0);
        try {
            BookingLifecycleEventDto event = objectMapper.readValue(row.getPayload(), BookingLifecycleEventDto.class);
            bookingLifecyclePublisher.publish(event);
            outboxRepository.delete(row);
            if (log.isDebugEnabled()) {
                log.debug("Relayed outbox id={}", row.getId());
            }
        } catch (JsonProcessingException e) {
            log.error("Outbox relay failed (invalid payload) for id={}, row will be retried", row.getId(), e);
        } catch (RuntimeException e) {
            log.error("Outbox relay failed for id={}, row will be retried", row.getId(), e);
        }
    }
}
