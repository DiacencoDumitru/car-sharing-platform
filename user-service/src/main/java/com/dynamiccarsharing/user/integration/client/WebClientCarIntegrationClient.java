package com.dynamiccarsharing.user.integration.client;

import com.dynamiccarsharing.user.integration.config.IntegrationClientProperties;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@Slf4j
public class WebClientCarIntegrationClient implements CarIntegrationClient {

    private final WebClient carWebClient;
    private final IntegrationClientProperties properties;

    public WebClientCarIntegrationClient(WebClient.Builder webClientBuilder, IntegrationClientProperties properties) {
        this.carWebClient = webClientBuilder.baseUrl("lb://car-service").build();
        this.properties = properties;
    }

    @Override
    public void assertCarExists(Long carId) {
        try {
            carWebClient.get()
                    .uri("/api/v1/cars/{id}", carId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError, response ->
                            response.createException().map(ex -> new ServiceException("Car service is unavailable", ex))
                    )
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .retryWhen(
                            Retry.backoff(properties.getRetryMaxAttempts(), Duration.ofMillis(properties.getRetryBackoffMillis()))
                                    .filter(this::isRetriable)
                    )
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new ValidationException("Car with ID " + carId + " does not exist or is unavailable.");
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to resolve car with ID " + carId, e);
        }
    }

    private boolean isRetriable(Throwable throwable) {
        return throwable instanceof WebClientRequestException
                || throwable instanceof ServiceException;
    }
}
