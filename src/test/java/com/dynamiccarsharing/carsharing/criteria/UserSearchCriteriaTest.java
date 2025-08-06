package com.dynamiccarsharing.carsharing.criteria;

import com.dynamiccarsharing.carsharing.dto.criteria.UserSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
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