package com.dynamiccarsharing.notification.notify;

import com.dynamiccarsharing.contracts.dto.BookingLifecycleEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StubEmailNotificationProvider implements EmailNotificationProvider {

    private final NotificationMessageRepository repository;
    private final WebClient.Builder webClientBuilder;

    @Value("${notifications.email.http.endpoint-url:}")
    private String endpointUrl;

    @Override
    public boolean sendForBooking(BookingLifecycleEventDto event, String toEmail) {
        if (event == null || event.getBookingId() == null || event.getBookingStatus() == null) {
            return false;
        }

        String bookingStatus = event.getBookingStatus().name();
        String subject = "Booking update: " + bookingStatus;
        String body = "Booking " + event.getBookingId() + " is now " + bookingStatus + ".";

        NotificationMessage message = NotificationMessage.builder()
                .bookingId(event.getBookingId())
                .renterId(event.getRenterId())
                .bookingStatus(event.getBookingStatus())
                .channel("EMAIL")
                .recipient(toEmail)
                .subject(subject)
                .body(body)
                .sentAt(Instant.now())
                .build();

        repository.save(message);

        log.info("Email notification saved (channel=EMAIL): bookingId={}, to={}", event.getBookingId(), toEmail);

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
                            "recipient", toEmail,
                            "channel", "EMAIL",
                            "subject", subject,
                            "body", body,
                            "sentAt", message.getSentAt().toString()
                    ))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception ex) {
            log.warn("HTTP email delivery failed (bookingId={}, to={}): {}", event.getBookingId(), toEmail, ex.toString());
        }

        return true;
    }
}

