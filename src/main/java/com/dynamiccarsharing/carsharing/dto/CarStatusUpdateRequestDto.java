package com.dynamiccarsharing.carsharing.dto;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import lombok.Data;

@Data
public class CarStatusUpdateRequestDto {
    private CarStatus status;
}