package com.dynamiccarsharing.car.integration.booking;

import com.dynamiccarsharing.contracts.dto.BookingForReviewDto;
import com.dynamiccarsharing.util.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Optional;

@Component
public class WebClientBookingIntegrationClient implements BookingIntegrationClient {

    private final WebClient bookingWebClient;
    private final BookingIntegrationClientProperties properties;

    public WebClientBookingIntegrationClient(WebClient.Builder webClientBuilder,
                                            BookingIntegrationClientProperties properties) {
        this.bookingWebClient = webClientBuilder.baseUrl("lb://booking-service").build();
        this.properties = properties;
    }

    @Override
    public Optional<BookingForReviewDto> getBookingForReview(Long bookingId) {
        try {
            BookingForReviewDto dto = bookingWebClient.get()
                    .uri("/api/v1/internal/bookings/{bookingId}/for-review", bookingId)
                    .retrieve()
                    .bodyToMono(BookingForReviewDto.class)
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .retryWhen(Retry.backoff(properties.getRetryMaxAttempts(), Duration.ofMillis(properties.getRetryBackoffMillis()))
                            .filter(WebClientRequestException.class::isInstance))
                    .block();
            return Optional.ofNullable(dto);
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw new ServiceException("Booking service error", e);
        }
    }
}
