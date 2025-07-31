package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends Repository<User, Long> {

    List<User> findByRole(UserRole role);

    List<User> findByStatus(UserStatus status);

    Optional<User> findByContactInfoEmail(String email);

    Optional<User> findWithCarsById(Long id);

    List<User> findByFilter(Filter<User> filter) throws SQLException;

    @Override
    List<User> findAll();
}