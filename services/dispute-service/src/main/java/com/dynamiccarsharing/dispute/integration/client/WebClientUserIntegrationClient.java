package com.dynamiccarsharing.dispute.integration.client;

import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.dispute.integration.config.IntegrationClientProperties;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.exception.ValidationException;
import com.dynamiccarsharing.util.web.ResilientWebClientExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@Slf4j
public class WebClientUserIntegrationClient implements UserIntegrationClient {

    private final WebClient userWebClient;
    private final ResilientWebClientExecutor resilientExecutor;

    public WebClientUserIntegrationClient(WebClient.Builder webClientBuilder, IntegrationClientProperties properties) {
        this.userWebClient = webClientBuilder.baseUrl("lb://user-service").build();
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
                            .uri("/api/v1/users/{id}", userId)
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
        } catch (ServiceException e) {
            throw e;
        }
    }
}
