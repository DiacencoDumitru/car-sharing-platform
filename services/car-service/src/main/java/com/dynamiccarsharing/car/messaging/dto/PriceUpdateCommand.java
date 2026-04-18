package com.dynamiccarsharing.car.messaging.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode
public class PriceUpdateCommand {
    private Long carId;
    private BigDecimal newPrice;
    private String requestId;
}
