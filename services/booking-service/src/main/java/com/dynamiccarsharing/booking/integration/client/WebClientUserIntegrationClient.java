package com.dynamiccarsharing.booking.integration.client;

import com.dynamiccarsharing.booking.integration.config.IntegrationClientProperties;
import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.exception.ValidationException;
import com.dynamiccarsharing.util.web.ResilientWebClientExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.Optional;

@Component
@Slf4j
public class WebClientUserIntegrationClient implements UserIntegrationClient {

    private static final String INTERNAL_USER_BY_ID_PATH = "/api/v1/internal/users/{id}";
    private static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    private final WebClient userWebClient;
    private final IntegrationClientProperties properties;
    private final ResilientWebClientExecutor resilientExecutor;

    public WebClientUserIntegrationClient(WebClient.Builder webClientBuilder, IntegrationClientProperties properties) {
        this.userWebClient = webClientBuilder.baseUrl("lb://user-service").build();
        this.properties = properties;
        this.resilientExecutor = new ResilientWebClientExecutor(
                properties.getTimeoutSeconds(),
                properties.getRetryMaxAttempts(),
                properties.getRetryBackoffMillis()
        );
    }

    @Override
    public void assertUserExists(Long userId) {
        try {
            UserDto user = resilientExecutor.execute(() -> userWebClient.get()
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
                            .bodyToMono(UserDto.class),
                    "Failed to validate user with ID " + userId);

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

    @Override
    public Optional<Long> findReferredByUserId(Long userId) {
        try {
            UserDto user = resilientExecutor.execute(() -> userWebClient.get()
                            .uri(INTERNAL_USER_BY_ID_PATH, userId)
                            .retrieve()
                            .onStatus(HttpStatusCode::is5xxServerError, response ->
                                    response.createException().map(ex -> new ServiceException("User service is unavailable", ex))
                            )
                            .bodyToMono(UserDto.class),
                    "Failed to load referral context for user " + userId);

            if (user == null || user.getReferredByUserId() == null) {
                return Optional.empty();
            }
            return Optional.of(user.getReferredByUserId());
        } catch (WebClientResponseException.NotFound e) {
            return Optional.empty();
        } catch (ServiceException e) {
            throw e;
        }
    }
}
