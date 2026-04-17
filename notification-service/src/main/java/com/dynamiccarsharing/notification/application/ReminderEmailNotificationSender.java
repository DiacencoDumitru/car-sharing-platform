package com.dynamiccarsharing.notification.application;

import com.dynamiccarsharing.contracts.dto.BookingReminderEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderEmailNotificationSender {

    @Value("${notifications.email.http.endpoint-url:}")
    private String endpointUrl;

    private final WebClient.Builder webClientBuilder;

    public void send(BookingReminderEventDto event, String toEmail) {
        String subject = "Rental reminder for booking " + event.getBookingId();
        String body = "Reminder (" + event.getReminderType() + ") for booking " + event.getBookingId()
                + ", car " + event.getCarId() + ".";
        log.info("Reminder email: bookingId={}, type={}, to={}", event.getBookingId(), event.getReminderType(), toEmail);
        if (endpointUrl == null || endpointUrl.isBlank()) {
            return;
        }
        try {
            webClientBuilder.build()
                    .post()
                    .uri(endpointUrl)
                    .bodyValue(Map.of(
                            "bookingId", event.getBookingId(),
                            "reminderType", event.getReminderType().name(),
                            "recipient", toEmail,
                            "channel", "EMAIL",
                            "subject", subject,
                            "body", body
                    ))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception ex) {
            log.warn("HTTP email reminder failed (bookingId={}): {}", event.getBookingId(), ex.toString());
        }
    }
}
