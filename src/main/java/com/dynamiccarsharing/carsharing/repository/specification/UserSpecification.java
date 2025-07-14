package com.dynamiccarsharing.carsharing.repository.specification;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.model.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> hasRole(UserRole role) {
        return (root, query, cb) -> cb.equal(root.get("role"), role);
    }

    public static Specification<User> hasStatus(UserStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<User> hasEmail(String email) {
        return (root, query, cb) -> cb.equal(root.join("contactInfo").get("email"), email);
    }
}