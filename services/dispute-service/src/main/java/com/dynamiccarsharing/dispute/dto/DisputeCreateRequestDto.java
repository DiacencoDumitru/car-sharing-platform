package com.dynamiccarsharing.dispute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DisputeCreateRequestDto {
    @NotBlank(message = "A description is required to file a dispute.")
    @Size(min = 20, max = 2000, message = "Description must be between 20 and 2000 characters.")
    private String description;
}