package com.dynamiccarsharing.contracts.dto;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private UserRole role;
    private UserStatus status;
    private ContactInfoDto contactInfo;
}