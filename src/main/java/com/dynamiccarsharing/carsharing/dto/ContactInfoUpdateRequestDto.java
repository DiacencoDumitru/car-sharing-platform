package com.dynamiccarsharing.carsharing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContactInfoUpdateRequestDto {
    @NotBlank(message = "First name must not be blank.")
    private String firstName;

    @NotBlank(message = "Last name must not be blank.")
    private String lastName;

    @NotBlank(message = "Email is required.")
    @Email(message = "Please provide a valid email address.")
    private String email;

    @NotBlank(message = "Phone number must not be blank.")
    private String phoneNumber;
}