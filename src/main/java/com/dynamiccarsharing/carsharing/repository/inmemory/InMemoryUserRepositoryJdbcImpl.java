package com.dynamiccarsharing.carsharing.repository.inmemory;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.UserRepository;
import com.dynamiccarsharing.carsharing.filter.Filter;

import java.util.*;

public class InMemoryUserRepositoryJdbcImpl implements UserRepository {
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
        return userMap.values().stream().filter(filter::test).toList();
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(userMap.values());
    }

    @Override
    public List<User> findByRole(UserRole role) {
        return userMap.values().stream()
                .filter(user -> user.getRole() == role)
                .toList();
    }

    @Override
    public List<User> findByStatus(UserStatus status) {
        return userMap.values().stream()
                .filter(user -> user.getStatus() == status)
                .toList();
    }

    @Override
    public Optional<User> findByContactInfoEmail(String email) {
        return userMap.values().stream()
                .filter(user -> user.getContactInfo() != null && user.getContactInfo().getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public Optional<User> findWithCarsById(Long id) {
        return Optional.empty();
    }
}