package com.dynamiccarsharing.car.integration.booking;

import com.dynamiccarsharing.contracts.dto.BookingForReviewDto;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.security.InternalApiKeyAuthenticationFilter;
import com.dynamiccarsharing.util.web.ResilientWebClientExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.Optional;

@Component
public class WebClientBookingIntegrationClient implements BookingIntegrationClient {

    private final WebClient bookingWebClient;
    private final BookingIntegrationClientProperties properties;
    private final ResilientWebClientExecutor resilientExecutor;

    public WebClientBookingIntegrationClient(WebClient.Builder webClientBuilder,
                                            BookingIntegrationClientProperties properties) {
        this.bookingWebClient = webClientBuilder.baseUrl("lb://booking-service").build();
        this.properties = properties;
        this.resilientExecutor = new ResilientWebClientExecutor(
                properties.getTimeoutSeconds(),
                properties.getRetryMaxAttempts(),
                properties.getRetryBackoffMillis()
        );
    }

    @Override
    public Optional<BookingForReviewDto> getBookingForReview(Long bookingId) {
        try {
            BookingForReviewDto dto = resilientExecutor.execute(() -> bookingWebClient.get()
                            .uri("/api/v1/internal/bookings/{bookingId}/for-review", bookingId)
                            .headers(this::applyInternalApiKeyIfConfigured)
                            .retrieve()
                            .bodyToMono(BookingForReviewDto.class),
                    "Booking service error");
            return Optional.ofNullable(dto);
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw new ServiceException("Booking service error", e);
        }
    }

    private void applyInternalApiKeyIfConfigured(HttpHeaders headers) {
        if (StringUtils.hasText(properties.getInternalApiKey())) {
            headers.set(
                    InternalApiKeyAuthenticationFilter.INTERNAL_API_KEY_HEADER,
                    properties.getInternalApiKey());
        }
    }
}
