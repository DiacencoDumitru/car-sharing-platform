package com.dynamiccarsharing.user.dto;

import com.dynamiccarsharing.contracts.enums.UserRole;
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