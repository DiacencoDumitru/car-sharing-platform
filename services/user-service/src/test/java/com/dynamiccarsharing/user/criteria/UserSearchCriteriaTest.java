package com.dynamiccarsharing.user.criteria;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserSearchCriteriaTest {

    @Test
    void builder_withAllFields_setsAndGetsAllFields() {
        String email = "test@example.com";
        UserRole role = UserRole.ADMIN;
        UserStatus status = UserStatus.ACTIVE;

        UserSearchCriteria criteria = UserSearchCriteria.builder()
                .email(email)
                .role(role)
                .status(status)
                .build();

        assertNotNull(criteria);
        assertEquals(email, criteria.getEmail());
        assertEquals(role, criteria.getRole());
        assertEquals(status, criteria.getStatus());
    }
}