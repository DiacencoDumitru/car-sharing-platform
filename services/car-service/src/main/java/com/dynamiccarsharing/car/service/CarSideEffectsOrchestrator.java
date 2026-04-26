package com.dynamiccarsharing.car.service;

import com.dynamiccarsharing.car.messaging.CarEventPublisher;
import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.search.es.CarSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class CarSideEffectsOrchestrator {

    private final CarSearchService carSearchService;
    private final CarEventPublisher carEventPublisher;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public void reindexCarSafely(Long carId) {
        CircuitBreaker cb = circuitBreakerFactory.create("carSearch");
        cb.run(() -> {
                    carSearchService.indexCar(carId);
                    return null;
                },
                ex -> {
                    log.warn("carSearch CB fallback: indexing car {} failed: {}", carId, ex.toString());
                    return null;
                }
        );
    }

    public void deleteIndexSafely(Long carId) {
        CircuitBreaker cb = circuitBreakerFactory.create("carSearch");
        cb.run(() -> {
                    carSearchService.deleteFromIndex(carId);
                    return null;
                },
                ex -> {
                    log.warn("carSearch CB fallback: delete index for car {} failed: {}", carId, ex.toString());
                    return null;
                }
        );
    }

    public void publishCarCreated(Car car) {
        publishEventSafely(() -> {
            carEventPublisher.publishCarCreated(car);
            return null;
        }, "CarCreated", car.getId());
    }

    public void publishCarUpdated(Car car) {
        publishEventSafely(() -> {
            carEventPublisher.publishCarUpdated(car);
            return null;
        }, "CarUpdated", car.getId());
    }

    public void publishCarDeleted(Long carId) {
        publishEventSafely(() -> {
            carEventPublisher.publishCarDeleted(carId);
            return null;
        }, "CarDeleted", carId);
    }

    private void publishEventSafely(Supplier<Void> publisher, String action, Long carId) {
        CircuitBreaker cb = circuitBreakerFactory.create("carEvents");
        cb.run(() -> {
                    publisher.get();
                    return null;
                },
                ex -> {
                    log.warn("carEvents CB fallback: publishing {} for car {} failed: {}", action, carId, ex.toString());
                    return null;
                }
        );
    }
}
