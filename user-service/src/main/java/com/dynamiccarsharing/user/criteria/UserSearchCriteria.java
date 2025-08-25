package com.dynamiccarsharing.user.criteria;

import lombok.Builder;
import lombok.Getter;
import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;

@Getter
@Builder
public class UserSearchCriteria {
    private String email;
    private UserRole role;
    private UserStatus status;
}