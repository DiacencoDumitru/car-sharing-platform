package com.dynamiccarsharing.user.integration.client;

import com.dynamiccarsharing.user.dto.me.BookingPageResponse;
import com.dynamiccarsharing.user.dto.me.TransactionPageResponse;
import com.dynamiccarsharing.user.integration.config.IntegrationClientProperties;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.security.InternalApiKeyAuthenticationFilter;
import com.dynamiccarsharing.util.web.ResilientWebClientExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import java.util.function.Function;

@Component
public class WebClientBookingMeClient implements BookingMeClient {

    private final WebClient bookingWebClient;
    private final ResilientWebClientExecutor resilientExecutor;
    private final String internalApiKey;

    public WebClientBookingMeClient(
            WebClient.Builder webClientBuilder,
            IntegrationClientProperties properties,
            @Value("${application.security.internal-api-key:}") String internalApiKey
    ) {
        this.bookingWebClient = webClientBuilder.baseUrl("lb://booking-service").build();
        this.internalApiKey = internalApiKey;
        this.resilientExecutor = new ResilientWebClientExecutor(
                properties.getTimeoutSeconds(),
                properties.getRetryMaxAttempts(),
                properties.getRetryBackoffMillis()
        );
    }

    @Override
    public BookingPageResponse getUserBookings(Long userId, String asRole, Pageable pageable) {
        BookingPageResponse response = resilientExecutor.execute(() -> bookingWebClient.get()
                        .uri(internalUserBookingsUri(userId, asRole, pageable))
                        .headers(this::applyInternalApiKeyIfConfigured)
                        .retrieve()
                        .bodyToMono(BookingPageResponse.class),
                "Booking service request for user bookings failed");
        if (response == null) {
            throw new ServiceException("Booking service returned empty response", new IllegalStateException());
        }
        return response;
    }

    @Override
    public TransactionPageResponse getUserTransactions(Long userId, Pageable pageable) {
        TransactionPageResponse response = resilientExecutor.execute(() -> bookingWebClient.get()
                        .uri(internalUserTransactionsUri(userId, pageable))
                        .headers(this::applyInternalApiKeyIfConfigured)
                        .retrieve()
                        .bodyToMono(TransactionPageResponse.class),
                "Booking service request for user transactions failed");
        if (response == null) {
            throw new ServiceException("Booking service returned empty response", new IllegalStateException());
        }
        return response;
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

    private void applyInternalApiKeyIfConfigured(HttpHeaders headers) {
        if (StringUtils.hasText(internalApiKey)) {
            headers.set(InternalApiKeyAuthenticationFilter.INTERNAL_API_KEY_HEADER, internalApiKey);
        }
    }
}
