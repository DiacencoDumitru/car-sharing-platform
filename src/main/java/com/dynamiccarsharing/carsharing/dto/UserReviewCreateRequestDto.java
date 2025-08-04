package com.dynamiccarsharing.carsharing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserReviewCreateRequestDto {
    @NotNull(message = "Reviewer ID cannot be null.")
    private Long reviewerId;

    @NotBlank(message = "Comment cannot be blank.")
    private String comment;
}