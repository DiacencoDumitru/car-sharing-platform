package com.dynamiccarsharing.carsharing.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LocationCreateRequestDto {
    @NotBlank(message = "City cannot be blank.")
    private String city;

    @NotBlank(message = "State cannot be blank.")
    private String state;

    @NotBlank(message = "Zip code cannot be blank.")
    private String zipCode;
}