package com.dynamiccarsharing.car.dto;

import com.dynamiccarsharing.contracts.enums.CarStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CarStatusUpdateRequestDto {
    @NotNull(message = "Status cannot be null.")
    private CarStatus status;
}