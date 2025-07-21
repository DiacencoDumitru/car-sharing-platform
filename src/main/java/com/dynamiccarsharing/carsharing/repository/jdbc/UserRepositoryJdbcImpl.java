package com.dynamiccarsharing.carsharing.repository.jdbc;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryJdbcImpl extends Repository<User, Long> {
    List<User> findByRole(UserRole role);

    List<User> findByStatus(UserStatus status);

    Optional<User> findByContactInfoEmail(String email);
}