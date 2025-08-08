package com.dynamiccarsharing.carsharing.dto;

import lombok.Data;

@Data
public class CarReviewDto {
    private Long id;
    private Long carId;
    private Long reviewerId;
    private String comment;
}