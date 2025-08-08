package com.dynamiccarsharing.carsharing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LocationUpdateRequestDto {
    @NotBlank(message = "City cannot be blank.")
    @Size(min = 2, max = 100, message = "City name must be between 2 and 100 characters.")
    private String city;

    @NotBlank(message = "State cannot be blank.")
    @Size(min = 2, max = 100, message = "State name must be between 2 and 100 characters.")
    private String state;

    @NotBlank(message = "Zip code cannot be blank.")
    @Pattern(regexp = "^\\d{5}$", message = "Zip code must be 5 digits.")
    private String zipCode;
}