package com.dynamiccarsharing.carsharing.dto.criteria;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.model.Location;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CarSearchCriteria {
    private String make;
    private String model;
    private CarStatus status;
    private Location location;
    private CarType type;
    private VerificationStatus verificationStatus;
}