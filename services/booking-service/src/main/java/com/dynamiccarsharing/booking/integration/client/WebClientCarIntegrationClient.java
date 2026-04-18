package com.dynamiccarsharing.booking.integration.client;

import com.dynamiccarsharing.booking.integration.config.IntegrationClientProperties;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.exception.ValidationException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public void assertCarAvailable(Long carId) {
        CarDto car = getCarById(carId);
        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new ValidationException("Car with ID " + carId + " is not available for booking.");
        }
    }

    @Override
    public CarDto getCarById(Long carId) {
        try {
            CarDto car = carWebClient.get()
                    .uri("/api/v1/cars/{id}", carId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError, response ->
                            response.createException().map(ex -> new ServiceException("Car service is unavailable", ex))
                    )
                    .bodyToMono(CarDto.class)
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .retryWhen(
                            Retry.backoff(properties.getRetryMaxAttempts(), Duration.ofMillis(properties.getRetryBackoffMillis()))
                                    .filter(this::isRetriable)
                    )
                    .block();

            if (car == null) {
                throw new ValidationException("Car with ID " + carId + " does not exist or is unavailable.");
            }
            log.info("Car lookup handled by instance: {}", car.getInstanceId());
            return car;
        } catch (WebClientResponseException.NotFound e) {
            throw new ValidationException("Car with ID " + carId + " does not exist or is unavailable.");
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to get car with ID " + carId, e);
        }
    }

    @Override
    public List<Long> findCarIdsByOwner(Long ownerId) {
        List<Long> ids = new ArrayList<>();
        int pageIndex = 0;
        Integer totalPages = null;
        do {
            final int p = pageIndex;
            CarPageResponse body = carWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/cars")
                            .queryParam("ownerId", ownerId)
                            .queryParam("size", 100)
                            .queryParam("page", p)
                            .build())
                    .retrieve()
                    .bodyToMono(CarPageResponse.class)
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .retryWhen(
                            Retry.backoff(properties.getRetryMaxAttempts(), Duration.ofMillis(properties.getRetryBackoffMillis()))
                                    .filter(this::isRetriable)
                    )
                    .block();
            if (body == null || body.getContent() == null || body.getContent().isEmpty()) {
                break;
            }
            ids.addAll(body.getContent().stream().map(CarDto::getId).filter(Objects::nonNull).toList());
            totalPages = body.getTotalPages();
            pageIndex++;
        } while (totalPages != null && pageIndex < totalPages);
        return ids;
    }

    private boolean isRetriable(Throwable throwable) {
        return throwable instanceof WebClientRequestException
                || throwable instanceof ServiceException;
    }

    @Data
    private static class CarPageResponse {
        private List<CarDto> content;
        private int totalPages;
    }
}
