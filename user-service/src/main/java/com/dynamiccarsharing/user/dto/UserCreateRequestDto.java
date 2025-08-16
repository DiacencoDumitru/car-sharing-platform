package com.dynamiccarsharing.contracts.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.dynamiccarsharing.contracts.enums.UserRole;

@Data
public class UserCreateRequestDto {
    @NotNull
    private UserRole role;

    @NotNull
    @Valid
    private ContactInfoCreateRequestDto contactInfo;
}