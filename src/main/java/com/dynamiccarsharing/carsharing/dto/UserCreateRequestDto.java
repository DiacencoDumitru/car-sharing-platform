package com.dynamiccarsharing.carsharing.dto;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserCreateRequestDto {
    @NotNull
    private UserRole role;

    @NotNull
    @Valid
    private ContactInfoCreateRequestDto contactInfo;
}