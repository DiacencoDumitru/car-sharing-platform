package com.dynamiccarsharing.notification.application;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.notification.analytics.BookingLifecycleAnalyticsEvent;
import com.dynamiccarsharing.notification.analytics.BookingLifecycleAnalyticsEventRepository;
import com.dynamiccarsharing.notification.fraud.AntiFraudService;
import com.dynamiccarsharing.notification.fraud.FraudDecision;
import com.dynamiccarsharing.notification.notify.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingLifecycleEventProcessor {

    private final BookingLifecycleAnalyticsEventRepository repository;
    private final AntiFraudService antiFraudService;
    private final NotificationDispatcher notificationDispatcher;

    @Transactional
    public void process(BookingLifecycleEventDto event) {
        if (event == null || event.getBookingId() == null || event.getBookingStatus() == null) {
            log.warn("Ignoring invalid BookingLifecycleEventDto (bookingId/status missing).");
            return;
        }

        Long bookingId = event.getBookingId();
        TransactionStatus status = event.getBookingStatus();

        if (repository.findByBookingIdAndBookingStatus(bookingId, status).isPresent()) {
            return;
        }

        Instant occurredAtForDecision = event.getOccurredAt() != null ? event.getOccurredAt() : Instant.now();
        BookingLifecycleEventDto eventForDecision = event;
        if (event.getOccurredAt() == null) {
            eventForDecision = BookingLifecycleEventDto.builder()
                    .bookingId(event.getBookingId())
                    .renterId(event.getRenterId())
                    .carId(event.getCarId())
                    .bookingStatus(event.getBookingStatus())
                    .occurredAt(occurredAtForDecision)
                    .build();
        }

        FraudDecision decision = antiFraudService.evaluate(eventForDecision);
        boolean notificationSent = notificationDispatcher.dispatchIfNeeded(event, decision);

        BookingLifecycleAnalyticsEvent analyticsEvent = BookingLifecycleAnalyticsEvent.builder()
                .bookingId(event.getBookingId())
                .renterId(event.getRenterId())
                .carId(event.getCarId())
                .bookingStatus(event.getBookingStatus())
                .occurredAt(occurredAtForDecision)
                .fraudRiskScore(decision != null ? decision.fraudRiskScore() : 0)
                .attentionRequired(decision != null && decision.attentionRequired())
                .notificationSent(notificationSent)
                .processedAt(Instant.now())
                .build();

        repository.save(analyticsEvent);
    }
}

