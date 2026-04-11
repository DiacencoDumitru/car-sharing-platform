package com.dynamiccarsharing.dispute.integration.client;

import com.dynamiccarsharing.contracts.dto.BookingDto;
import com.dynamiccarsharing.dispute.integration.config.IntegrationClientProperties;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.exception.ValidationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class WebClientBookingIntegrationClient implements BookingIntegrationClient {

    private final WebClient bookingWebClient;
    private final IntegrationClientProperties properties;

    public WebClientBookingIntegrationClient(WebClient.Builder webClientBuilder, IntegrationClientProperties properties) {
        this.bookingWebClient = webClientBuilder.baseUrl("lb://booking-service").build();
        this.properties = properties;
    }

    @Override
    public void assertBookingExists(Long bookingId) {
        try {
            BookingDto booking = bookingWebClient.get()
                    .uri("/api/v1/bookings/{id}", bookingId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError, response ->
                            response.createException().map(ex -> new ServiceException("Booking service is unavailable", ex))
                    )
                    .bodyToMono(BookingDto.class)
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .retryWhen(
                            Retry.backoff(properties.getRetryMaxAttempts(), Duration.ofMillis(properties.getRetryBackoffMillis()))
                                    .filter(this::isRetriable)
                    )
                    .block();

            if (booking == null) {
                throw new ValidationException("Booking with ID " + bookingId + " does not exist.");
            }
        } catch (WebClientResponseException.NotFound e) {
            throw new ValidationException("Booking with ID " + bookingId + " does not exist.");
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to validate booking with ID " + bookingId, e);
        }
    }

    private boolean isRetriable(Throwable throwable) {
        return throwable instanceof WebClientRequestException
                || throwable instanceof ServiceException;
    }
}
