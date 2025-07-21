package com.dynamiccarsharing.carsharing.repository.inmemory;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.jdbc.UserRepositoryJdbcImpl;
import com.dynamiccarsharing.carsharing.filter.Filter;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryUserRepositoryJdbcImpl implements UserRepositoryJdbcImpl {
    private final Map<Long, User> userMap = new HashMap<>();

    @Override
    public User save(User user) {
        userMap.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userMap.get(id));
    }

    @Override
    public void deleteById(Long id) {
        userMap.remove(id);
    }

    @Override
    public List<User> findByFilter(Filter<User> filter) {
        return userMap.values().stream().filter(filter::test).collect(Collectors.toList());
    }

    @Override
    public Iterable<User> findAll() {
        return userMap.values();
    }

    @Override
    public List<User> findByRole(UserRole role) {
        return userMap.values().stream()
                .filter(user -> user.getRole() == role)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByStatus(UserStatus status) {
        return userMap.values().stream()
                .filter(user -> user.getStatus() == status)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findByContactInfoEmail(String email) {
        return userMap.values().stream()
                .filter(user -> user.getContactInfo() != null && user.getContactInfo().getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}