package com.dynamiccarsharing.contracts.dto;

import com.dynamiccarsharing.contracts.enums.CarStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarUpdateRequestDto {
    @Size(min = 2, max = 20, message = "Registration number must be between 2 and 20 characters.")
    private String registrationNumber;

    @Size(min = 2, max = 50, message = "Make must be between 2 and 50 characters.")
    private String make;

    @Size(min = 2, max = 50, message = "Model must be between 2 and 50 characters.")
    private String model;

    private Long locationId;

    @DecimalMin(value = "0.01", message = "Price must be greater than zero.")
    private BigDecimal price;

    private CarStatus status;
}