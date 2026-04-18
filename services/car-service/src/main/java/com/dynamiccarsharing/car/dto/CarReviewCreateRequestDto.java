package com.dynamiccarsharing.car.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CarReviewCreateRequestDto {

    private Long carId;

    @NotNull
    private Long reviewerId;

    @NotNull
    private Long bookingId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @NotBlank(message = "Comment cannot be blank.")
    @Size(min = 10, max = 1000, message = "Comment must be between 10 and 1000 characters.")
    private String comment;
}