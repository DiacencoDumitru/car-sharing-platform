package com.dynamiccarsharing.notification.notify;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;

import com.dynamiccarsharing.notification.notify.NotificationMessage;
import com.dynamiccarsharing.notification.notify.NotificationMessageRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class HttpPushNotificationProvider implements PushNotificationProvider {

    private final NotificationMessageRepository repository;
    private final WebClient.Builder webClientBuilder;

    @Value("${notifications.push.http.endpoint-url:}")
    private String endpointUrl;

    @Override
    public boolean sendForBooking(BookingLifecycleEventDto event, String phoneNumber) {
        if (event == null || event.getBookingId() == null || event.getBookingStatus() == null || phoneNumber == null) {
            return false;
        }

        String bookingStatus = event.getBookingStatus().name();
        String subject = "Booking update: " + bookingStatus;
        String body = "Booking " + event.getBookingId() + " is now " + bookingStatus + ".";

        NotificationMessage message = NotificationMessage.builder()
                .bookingId(event.getBookingId())
                .renterId(event.getRenterId())
                .bookingStatus(event.getBookingStatus())
                .channel("PUSH")
                .recipient(phoneNumber)
                .subject(subject)
                .body(body)
                .sentAt(Instant.now())
                .build();

        repository.save(message);
        log.info("Push notification saved: bookingId={}, to={}", event.getBookingId(), phoneNumber);

        if (endpointUrl == null || endpointUrl.isBlank()) {
            return true;
        }

        try {
            webClientBuilder.build()
                    .post()
                    .uri(endpointUrl)
                    .bodyValue(Map.of(
                            "bookingId", event.getBookingId(),
                            "renterId", event.getRenterId(),
                            "bookingStatus", bookingStatus,
                            "recipient", phoneNumber,
                            "channel", "PUSH",
                            "subject", subject,
                            "body", body,
                            "sentAt", message.getSentAt().toString()
                    ))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception ex) {
            log.warn("HTTP push delivery failed (bookingId={}, to={}): {}", event.getBookingId(), phoneNumber, ex.toString());
        }

        return true;
    }
}

