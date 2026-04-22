package com.dynamiccarsharing.car.criteria;

import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.CarType;
import com.dynamiccarsharing.contracts.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarSearchCriteria {
    private String make;
    private String model;
    private Long ownerId;
    private Long locationId;
    private CarType type;
    private VerificationStatus verificationStatus;

    private List<CarStatus> statusIn;

    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private BigDecimal minAverageRating;

    private Integer minReviewCount;
}