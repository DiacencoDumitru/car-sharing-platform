package com.dynamiccarsharing.car.messaging.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PriceUpdateCommand {
    private Long carId;
    private BigDecimal newPrice;
    private String requestId; // optional tracing/correlation
}
