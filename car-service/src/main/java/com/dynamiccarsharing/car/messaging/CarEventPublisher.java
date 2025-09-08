package com.dynamiccarsharing.car.messaging;

import com.dynamiccarsharing.car.messaging.dto.CarEvent;
import com.dynamiccarsharing.car.model.Car;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class CarEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${application.messaging.topics.car-events:car.events}")
    private String carEventsTopic;

    public void publishCarCreated(Car car) {
        publish(build(car, "CAR_CREATED"));
    }

    public void publishCarUpdated(Car car) {
        publish(build(car, "CAR_UPDATED"));
    }

    public void publishCarDeleted(Long carId) {
        CarEvent e = CarEvent.builder()
                .type("CAR_DELETED")
                .carId(carId)
                .occurredAt(Instant.now())
                .build();
        publish(e);
    }

    private CarEvent build(Car car, String type) {
        return CarEvent.builder()
                .type(type)
                .carId(car.getId())
                .occurredAt(Instant.now())
                .ownerId(car.getOwnerId())
                .locationId(car.getLocation() != null ? car.getLocation().getId() : null)
                .make(car.getMake())
                .model(car.getModel())
                .status(car.getStatus() != null ? car.getStatus().name() : null)
                .verificationStatus(car.getVerificationStatus() != null ? car.getVerificationStatus().name() : null)
                .typeEnum(car.getType() != null ? car.getType().name() : null)
                .pricePerDay(car.getPrice() != null ? car.getPrice().doubleValue() : null)
                .build();
    }

    private void publish(CarEvent event) {
        String key = event.getCarId() != null ? String.valueOf(event.getCarId()) : "car";
        kafkaTemplate.send(carEventsTopic, key, event)
                .whenComplete((md, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish {} for carId={}", event.getType(), event.getCarId(), ex);
                    } else {
                        log.info("Published {} for carId={} to {}@{}:{}", event.getType(), event.getCarId(),
                                carEventsTopic, md.getRecordMetadata().partition(), md.getRecordMetadata().offset());
                    }
                });
    }
}
