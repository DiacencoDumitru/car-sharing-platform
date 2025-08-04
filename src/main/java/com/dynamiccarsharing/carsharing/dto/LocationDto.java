package com.dynamiccarsharing.carsharing.dto;

import lombok.Data;

@Data
public class LocationDto {
    private Long id;
    private String city;
    private String state;
    private String zipCode;
}