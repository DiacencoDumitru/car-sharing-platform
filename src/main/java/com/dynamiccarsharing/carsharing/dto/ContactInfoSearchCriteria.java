package com.dynamiccarsharing.carsharing.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContactInfoSearchCriteria {
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String email;
}