package com.dynamiccarsharing.dispute.integration.client;

import com.dynamiccarsharing.contracts.dto.BookingDto;
import com.dynamiccarsharing.dispute.integration.config.IntegrationClientProperties;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.exception.ValidationException;
import com.dynamiccarsharing.util.web.ResilientWebClientExecutor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class WebClientBookingIntegrationClient implements BookingIntegrationClient {

    private final WebClient bookingWebClient;
    private final ResilientWebClientExecutor resilientExecutor;

    public WebClientBookingIntegrationClient(WebClient.Builder webClientBuilder, IntegrationClientProperties properties) {
        this.bookingWebClient = webClientBuilder.baseUrl("lb://booking-service").build();
        this.resilientExecutor = new ResilientWebClientExecutor(
                properties.getTimeoutSeconds(),
                properties.getRetryMaxAttempts(),
                properties.getRetryBackoffMillis()
        );
    }

    @Override
    public void assertBookingExists(Long bookingId) {
        try {
            BookingDto booking = resilientExecutor.execute(() -> bookingWebClient.get()
                            .uri("/api/v1/bookings/{id}", bookingId)
                            .retrieve()
                            .onStatus(HttpStatusCode::is5xxServerError, response ->
                                    response.createException().map(ex -> new ServiceException("Booking service is unavailable", ex))
                            )
                            .bodyToMono(BookingDto.class),
                    "Failed to validate booking with ID " + bookingId);

            if (booking == null) {
                throw new ValidationException("Booking with ID " + bookingId + " does not exist.");
            }
        } catch (WebClientResponseException.NotFound e) {
            throw new ValidationException("Booking with ID " + bookingId + " does not exist.");
        } catch (ValidationException e) {
            throw e;
        } catch (ServiceException e) {
            throw e;
        }
    }
}
