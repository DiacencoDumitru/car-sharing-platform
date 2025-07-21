package com.dynamiccarsharing.carsharing.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocationSearchCriteria {
    private String city;
    private String state;
    private String zipCode;
}