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
public class ReminderPushNotificationSender {

    @Value("${notifications.push.http.endpoint-url:}")
    private String endpointUrl;

    private final WebClient.Builder webClientBuilder;

    public void send(BookingReminderEventDto event, String phoneNumber) {
        String body = "Reminder (" + event.getReminderType() + ") for booking " + event.getBookingId() + ".";
        log.info("Reminder push: bookingId={}, type={}, to={}", event.getBookingId(), event.getReminderType(), phoneNumber);
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
                            "recipient", phoneNumber,
                            "channel", "PUSH",
                            "body", body
                    ))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception ex) {
            log.warn("HTTP push reminder failed (bookingId={}): {}", event.getBookingId(), ex.toString());
        }
    }
}
