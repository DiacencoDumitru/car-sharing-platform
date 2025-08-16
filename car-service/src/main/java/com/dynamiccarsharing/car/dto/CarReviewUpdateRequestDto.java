package com.dynamiccarsharing.contracts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CarReviewUpdateRequestDto {
    @NotBlank(message = "Comment cannot be blank.")
    @Size(min = 10, max = 1000, message = "Comment must be between 10 and 1000 characters.")
    private String comment;
}