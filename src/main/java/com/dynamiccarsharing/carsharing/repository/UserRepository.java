package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UserRepository implements Repository<User> {
    private final Map<Long, User> usersById = new HashMap<>();
    private final Map<String, User> usersByEmail = new HashMap<>();

    @Override
    public void save(User user) {
        usersById.put(user.getId(), user);
        usersByEmail.put(user.getEmail(), user);
    }

    @Override
    public User findById(Long id) {
        return usersById.get(id);
    }

    @Override
    public User findByField(String fieldValue) {
        return usersByEmail.get(fieldValue);
    }

    @Override
    public void update(User user) {
        if (usersById.containsKey(user.getId())) {
            usersByEmail.remove(usersById.get(user.getId()).getEmail());
            usersById.put(user.getId(), user);
            usersByEmail.put(user.getEmail(), user);
        }
    }

    @Override
    public void delete(Long id) {
        User user = usersById.get(id);
        usersById.remove(id);
        usersByEmail.remove(user.getEmail());
    }

    @Override
    public Map<Long, User> findAll() {
        return new HashMap<>(usersById);
    }

    public Map<Long, User> findByFilter(String field, String value) {
        return usersById.entrySet().stream()
                .filter(entry -> {
                    User user = entry.getValue();
                    return (field.equals("name") && user.getName().equals(value)) ||
                            (field.equals("email") && user.getEmail().equals(value)) ||
                            (field.equals("phoneNumber") && user.getPhoneNumber().equals(value)) ||
                            (field.equals("role") && user.getRole().equals(value)) ||
                            (field.equals("status") && user.getStatus().equals(value));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
