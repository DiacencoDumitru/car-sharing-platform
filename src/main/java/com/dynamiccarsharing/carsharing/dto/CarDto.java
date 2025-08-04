package com.dynamiccarsharing.carsharing.dto;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CarDto {
    private Long id;
    private String registrationNumber;
    private String make;
    private String model;
    private CarStatus status;
    private Long locationId;
    private BigDecimal price;
    private CarType type;
    private VerificationStatus verificationStatus;
}