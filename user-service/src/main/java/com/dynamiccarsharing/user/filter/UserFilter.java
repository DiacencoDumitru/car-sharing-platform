package com.dynamiccarsharing.user.filter;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.util.filter.Filter;
import lombok.Getter;

@Getter
public class UserFilter implements Filter<User> {
    private final UserRole role;
    private final UserStatus status;
    private final String email;

    private UserFilter(UserRole role, UserStatus status, String email) {
        this.role = role;
        this.status = status;
        this.email = email;
    }

    public static UserFilter of(UserRole role, UserStatus status, String email) {
        return new UserFilter(role, status, email);
    }

    public static UserFilter ofRole(UserRole role) {
        return new UserFilter(role, null, null);
    }

    public static UserFilter ofStatus(UserStatus status) {
        return new UserFilter(null, status, null);
    }

    public static UserFilter ofEmail(String email) {
        return new UserFilter(null, null, email);
    }

    @Override
    public boolean test(User user) {
        boolean matches = true;
        if (role != null) matches &= user.getRole() == role;
        if (status != null) matches &= user.getStatus() == status;
        if (email != null) matches &= user.getContactInfo().getEmail().contains(email);
        return matches;
    }
}