package com.dynamiccarsharing.carsharing.dto;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private UserRole role;
    private UserStatus status;
    private ContactInfoDto contactInfo;
}