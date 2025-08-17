package com.dynamiccarsharing.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.dynamiccarsharing.contracts.enums.UserStatus;

@Data
public class UserStatusUpdateRequestDto {
    @NotNull
    private UserStatus status;
}