package com.dynamiccarsharing.user.integration.client;

import com.dynamiccarsharing.user.integration.config.IntegrationClientProperties;
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
public class WebClientCarIntegrationClient implements CarIntegrationClient {

    private final WebClient carWebClient;
    private final ResilientWebClientExecutor resilientExecutor;

    public WebClientCarIntegrationClient(WebClient.Builder webClientBuilder, IntegrationClientProperties properties) {
        this.carWebClient = webClientBuilder.baseUrl("lb://car-service").build();
        this.resilientExecutor = new ResilientWebClientExecutor(
                properties.getTimeoutSeconds(),
                properties.getRetryMaxAttempts(),
                properties.getRetryBackoffMillis()
        );
    }

    @Override
    public void assertCarExists(Long carId) {
        try {
            resilientExecutor.execute(() -> carWebClient.get()
                            .uri("/api/v1/cars/{id}", carId)
                            .retrieve()
                            .onStatus(HttpStatusCode::is5xxServerError, response ->
                                    response.createException().map(ex -> new ServiceException("Car service is unavailable", ex))
                            )
                            .toBodilessEntity()
                            .thenReturn(Boolean.TRUE),
                    "Failed to resolve car with ID " + carId);
        } catch (WebClientResponseException.NotFound e) {
            throw new ValidationException("Car with ID " + carId + " does not exist or is unavailable.");
        } catch (ValidationException e) {
            throw e;
        } catch (ServiceException e) {
            throw e;
        }
    }
}
