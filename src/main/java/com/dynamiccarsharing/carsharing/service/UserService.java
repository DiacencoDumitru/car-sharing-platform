package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.UserRepository;

import java.util.Map;

public class UserService {

    private final UserRepository userRepository = new UserRepository();

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public User findUserById(Long id) {
        return userRepository.findById(id);
    }

    public User findUserByEmail(String email) {
        return userRepository.findByField(email);
    }

    public void updateUser(User user) {
        userRepository.update(user);
    }

    public void deleteUser(Long id) {
        userRepository.delete(id);
    }

    public Map<Long, User> findAllUsers() {
        return userRepository.findAll();
    }

    public Map<Long, User> findUsersByFilter(String field, String value) {
        return userRepository.findByFilter(field, value);
    }

}
