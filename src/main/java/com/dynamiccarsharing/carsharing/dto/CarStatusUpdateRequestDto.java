package com.dynamiccarsharing.carsharing.dto;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CarStatusUpdateRequestDto {
    @NotNull(message = "Status cannot be null.")
    private CarStatus status;
}