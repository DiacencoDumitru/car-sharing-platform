package com.dynamiccarsharing.booking.integration.client;

import com.dynamiccarsharing.booking.integration.config.IntegrationClientProperties;
import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@Slf4j
public class WebClientUserIntegrationClient implements UserIntegrationClient {

    private static final String INTERNAL_USER_BY_ID_PATH = "/api/v1/internal/users/{id}";
    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    private final WebClient userWebClient;
    private final IntegrationClientProperties properties;

    public WebClientUserIntegrationClient(WebClient.Builder webClientBuilder, IntegrationClientProperties properties) {
        this.userWebClient = webClientBuilder.baseUrl("lb://user-service").build();
        this.properties = properties;
    }

    @Override
    public void assertUserExists(Long userId) {
        try {
            UserDto user = userWebClient.get()
                    .uri(INTERNAL_USER_BY_ID_PATH, userId)
                    .headers(h -> {
                        if (StringUtils.hasText(properties.getInternalApiKey())) {
                            h.set(INTERNAL_API_KEY_HEADER, properties.getInternalApiKey());
                        }
                    })
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError, response ->
                            response.createException().map(ex -> new ServiceException("User service is unavailable", ex))
                    )
                    .bodyToMono(UserDto.class)
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .retryWhen(
                            Retry.backoff(properties.getRetryMaxAttempts(), Duration.ofMillis(properties.getRetryBackoffMillis()))
                                    .filter(this::isRetriable)
                    )
                    .block();

            if (user != null) {
                log.info("User validation handled by instance: {}", user.getInstanceId());
                return;
            }
            throw new ValidationException("User with ID " + userId + " does not exist.");
        } catch (WebClientResponseException.NotFound e) {
            throw new ValidationException("User with ID " + userId + " does not exist.");
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to validate user with ID " + userId, e);
        }
    }

    private boolean isRetriable(Throwable throwable) {
        return throwable instanceof WebClientRequestException
                || throwable instanceof ServiceException;
    }
}
