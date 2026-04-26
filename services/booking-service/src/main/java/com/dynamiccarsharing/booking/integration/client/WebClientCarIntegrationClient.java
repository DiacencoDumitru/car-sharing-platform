package com.dynamiccarsharing.booking.integration.client;

import com.dynamiccarsharing.booking.integration.config.IntegrationClientProperties;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.exception.ValidationException;
import com.dynamiccarsharing.util.web.ResilientWebClientExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public void assertCarAvailable(Long carId) {
        CarDto car = getCarById(carId);
        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new ValidationException("Car with ID " + carId + " is not available for booking.");
        }
    }

    @Override
    public CarDto getCarById(Long carId) {
        try {
            CarDto car = resilientExecutor.execute(() -> carWebClient.get()
                            .uri("/api/v1/cars/{id}", carId)
                            .retrieve()
                            .onStatus(HttpStatusCode::is5xxServerError, response ->
                                    response.createException().map(ex -> new ServiceException("Car service is unavailable", ex))
                            )
                            .bodyToMono(CarDto.class),
                    "Failed to get car with ID " + carId);

            if (car == null) {
                throw new ValidationException("Car with ID " + carId + " does not exist or is unavailable.");
            }
            log.info("Car lookup handled by instance: {}", car.getInstanceId());
            return car;
        } catch (WebClientResponseException.NotFound e) {
            throw new ValidationException("Car with ID " + carId + " does not exist or is unavailable.");
        } catch (ValidationException e) {
            throw e;
        } catch (ServiceException e) {
            throw e;
        }
    }

    @Override
    public List<Long> findCarIdsByOwner(Long ownerId) {
        List<Long> ids = new ArrayList<>();
        int pageIndex = 0;
        Integer totalPages = null;
        do {
            final int p = pageIndex;
            CarPageResponse body = resilientExecutor.execute(() -> carWebClient.get()
                            .uri(uriBuilder -> uriBuilder.path("/api/v1/cars")
                                    .queryParam("ownerId", ownerId)
                                    .queryParam("size", 100)
                                    .queryParam("page", p)
                                    .build())
                            .retrieve()
                            .bodyToMono(CarPageResponse.class),
                    "Failed to list cars for owner " + ownerId);
            if (body == null || body.getContent() == null || body.getContent().isEmpty()) {
                break;
            }
            ids.addAll(body.getContent().stream().map(CarDto::getId).filter(Objects::nonNull).toList());
            totalPages = body.getTotalPages();
            pageIndex++;
        } while (totalPages != null && pageIndex < totalPages);
        return ids;
    }

    @Data
    private static class CarPageResponse {
        private List<CarDto> content;
        private int totalPages;
    }
}
