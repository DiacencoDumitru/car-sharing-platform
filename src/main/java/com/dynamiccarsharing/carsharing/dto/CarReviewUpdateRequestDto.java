package com.dynamiccarsharing.carsharing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CarReviewUpdateRequestDto {
    @NotBlank
    private String comment;
}