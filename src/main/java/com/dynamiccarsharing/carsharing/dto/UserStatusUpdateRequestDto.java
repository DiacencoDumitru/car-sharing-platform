package com.dynamiccarsharing.carsharing.dto;

import com.dynamiccarsharing.carsharing.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusUpdateRequestDto {
    @NotNull
    private UserStatus status;
}