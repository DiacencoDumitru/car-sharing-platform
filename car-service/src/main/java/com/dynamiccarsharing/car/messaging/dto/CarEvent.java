package com.dynamiccarsharing.car.messaging.dto;

import lombok.*;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode
public class CarEvent {
    private String type;
    private Long carId;
    private Instant occurredAt;
    private Long ownerId;
    private Long locationId;
    private String make;
    private String model;
    private String status;
    private String verificationStatus;
    private String typeEnum;
    private Double pricePerDay;
}
