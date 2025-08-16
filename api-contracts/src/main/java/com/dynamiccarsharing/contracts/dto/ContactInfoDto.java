package com.dynamiccarsharing.contracts.dto;

import lombok.Data;

@Data
public class ContactInfoDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
}