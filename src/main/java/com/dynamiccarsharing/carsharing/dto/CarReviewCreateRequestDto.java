package com.dynamiccarsharing.carsharing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CarReviewCreateRequestDto {

    private Long carId;

    @NotNull
    private Long reviewerId;
    @NotBlank
    private String comment;
}