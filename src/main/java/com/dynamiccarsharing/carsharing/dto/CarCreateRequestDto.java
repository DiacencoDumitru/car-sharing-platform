package com.dynamiccarsharing.carsharing.dto;

import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.validation.ValidRegistrationNumber;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarCreateRequestDto {
    @NotBlank(message = "Registration number cannot be blank.")
    @ValidRegistrationNumber
    private String registrationNumber;

    @NotBlank(message = "Make cannot be blank.")
    @Size(min = 2, max = 50, message = "Make must be between 2 and 50 characters.")
    private String make;

    @NotBlank(message = "Model cannot be blank.")
    @Size(min = 2, max = 50, message = "Model must be between 2 and 50 characters.")
    private String model;

    @NotNull(message = "Location ID cannot be null.")
    private Long locationId;

    @NotNull(message = "Price cannot be null.")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero.")
    private BigDecimal price;

    @NotNull(message = "Car type must be specified.")
    private CarType type;
}