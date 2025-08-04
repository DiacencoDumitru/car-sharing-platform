package com.dynamiccarsharing.carsharing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DisputeCreateRequestDto {
    @NotBlank(message = "A description is required to file a dispute.")
    private String description;
}