package com.dynamiccarsharing.contracts.dto;

import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.CarType;
import com.dynamiccarsharing.contracts.enums.VerificationStatus;
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
    private Long ownerId;
    private BigDecimal price;
    private CarType type;
    private VerificationStatus verificationStatus;
    private String instanceId;
    private BigDecimal averageRating;
    private Integer reviewCount;
}