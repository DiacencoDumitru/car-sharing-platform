package com.dynamiccarsharing.carsharing.dto;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarUpdateRequestDto {
    private String registrationNumber;
    private String make;
    private String model;

    private Long locationId;
    @Positive
    private BigDecimal price;
    private CarStatus status;
}