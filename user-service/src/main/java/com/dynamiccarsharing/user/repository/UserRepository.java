package com.dynamiccarsharing.user.repository;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.util.filter.Filter;
import com.dynamiccarsharing.util.repository.Repository;

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