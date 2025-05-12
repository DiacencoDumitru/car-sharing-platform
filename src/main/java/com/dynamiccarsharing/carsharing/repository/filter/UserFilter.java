package com.dynamiccarsharing.carsharing.repository.filter;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.model.User;

public class UserFilter implements Filter<User> {
    private UserRole role;
    private UserStatus status;
    private String email;

    public UserFilter setRole(UserRole role) {
        this.role = role;
        return this;
    }

    public UserFilter setStatus(UserStatus status) {
        this.status = status;
        return this;
    }

    public UserFilter setEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    public boolean test(User user) {
        boolean matches = true;
        if (role != null) matches &= user.getRole() == role;
        if (status != null) matches &= user.getStatus() == status;
        if (email != null) matches &= user.getContactInfo().getEmail().contains(email); // Partial match
        return matches;
    }
}
