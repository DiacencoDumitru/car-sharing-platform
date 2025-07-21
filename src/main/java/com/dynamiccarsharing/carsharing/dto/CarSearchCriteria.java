package com.dynamiccarsharing.carsharing.dto;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CarSearchCriteria {
    private String make;
    private String model;
    private CarStatus status;
    private Long locationId;
    private CarType type;
    private VerificationStatus verificationStatus;
}