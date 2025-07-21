package com.dynamiccarsharing.carsharing.dto;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSearchCriteria {
    private String email;
    private UserRole role;
    private UserStatus status;
}