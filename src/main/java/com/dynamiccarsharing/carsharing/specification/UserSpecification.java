package com.dynamiccarsharing.carsharing.specification;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.model.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> hasRole(UserRole role) {
        return (root, query, cb) -> role != null ? cb.equal(root.get("role"), role) : null;
    }

    public static Specification<User> hasStatus(UserStatus status) {
        return (root, query, cb) -> status != null ? cb.equal(root.get("status"), status) : null;
    }

    public static Specification<User> hasEmail(String email) {
        return (root, query, cb) -> email != null ? cb.equal(root.join("contactInfo").get("email"), email) : null;
    }

    public static Specification<User> withCriteria(String email, UserRole role, UserStatus status) {
        return Specification
                .where(hasEmail(email))
                .and(hasRole(role))
                .and(hasStatus(status));
    }
}