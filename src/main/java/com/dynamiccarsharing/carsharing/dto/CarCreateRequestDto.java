package com.dynamiccarsharing.carsharing.dto;

import com.dynamiccarsharing.carsharing.enums.CarType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CarCreateRequestDto {
    @NotNull
    private String registrationNumber;
    @NotNull
    private String make;
    @NotNull
    private String model;
    @NotNull
    private Long locationId;
    @NotNull @Positive
    private BigDecimal price;
    @NotNull
    private CarType type;
}