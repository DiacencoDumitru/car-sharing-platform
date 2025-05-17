package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.filter.UserFilter;

import java.util.*;

public class InMemoryUserRepository implements UserRepository {
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
    public Iterable<User> findAll() {
        return userMap.values();
    }

    public List<User> findByFilter(UserFilter filter) {
        return userMap.values().stream().filter(filter::test).toList();
    }
}
