package com.dynamiccarsharing.user.integration.client;

import com.dynamiccarsharing.user.dto.me.BookingPageResponse;
import com.dynamiccarsharing.user.dto.me.TransactionPageResponse;
import com.dynamiccarsharing.user.integration.config.IntegrationClientProperties;
import com.dynamiccarsharing.util.exception.ServiceException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.util.UriBuilder;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.function.Function;

@Component
public class WebClientBookingMeClient implements BookingMeClient {

    private final WebClient bookingWebClient;
    private final IntegrationClientProperties properties;

    public WebClientBookingMeClient(WebClient.Builder webClientBuilder, IntegrationClientProperties properties) {
        this.bookingWebClient = webClientBuilder.baseUrl("lb://booking-service").build();
        this.properties = properties;
    }

    @Override
    public BookingPageResponse getUserBookings(Long userId, String asRole, Pageable pageable) {
        return bookingWebClient.get()
                .uri(internalUserBookingsUri(userId, asRole, pageable))
                .retrieve()
                .bodyToMono(BookingPageResponse.class)
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .retryWhen(Retry.backoff(properties.getRetryMaxAttempts(), Duration.ofMillis(properties.getRetryBackoffMillis()))
                        .filter(WebClientRequestException.class::isInstance))
                .blockOptional()
                .orElseThrow(() -> new ServiceException("Booking service returned empty response", new IllegalStateException()));
    }

    @Override
    public TransactionPageResponse getUserTransactions(Long userId, Pageable pageable) {
        return bookingWebClient.get()
                .uri(internalUserTransactionsUri(userId, pageable))
                .retrieve()
                .bodyToMono(TransactionPageResponse.class)
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .retryWhen(Retry.backoff(properties.getRetryMaxAttempts(), Duration.ofMillis(properties.getRetryBackoffMillis()))
                        .filter(WebClientRequestException.class::isInstance))
                .blockOptional()
                .orElseThrow(() -> new ServiceException("Booking service returned empty response", new IllegalStateException()));
    }

    private Function<UriBuilder, java.net.URI> internalUserBookingsUri(Long userId, String asRole, Pageable pageable) {
        return uriBuilder -> {
            UriBuilder ub = uriBuilder.path("/api/v1/internal/users/{userId}/bookings")
                    .queryParam("page", pageable.getPageNumber())
                    .queryParam("size", pageable.getPageSize());
            if (asRole != null && !asRole.isBlank()) {
                ub.queryParam("asRole", asRole);
            }
            for (Sort.Order o : pageable.getSort()) {
                ub.queryParam("sort", o.getProperty() + "," + o.getDirection().name().toLowerCase());
            }
            return ub.build(userId);
        };
    }

    private Function<UriBuilder, java.net.URI> internalUserTransactionsUri(Long userId, Pageable pageable) {
        return uriBuilder -> {
            UriBuilder ub = uriBuilder.path("/api/v1/internal/users/{userId}/transactions")
                    .queryParam("page", pageable.getPageNumber())
                    .queryParam("size", pageable.getPageSize());
            for (Sort.Order o : pageable.getSort()) {
                ub.queryParam("sort", o.getProperty() + "," + o.getDirection().name().toLowerCase());
            }
            return ub.build(userId);
        };
    }
}
