package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.UserRepository;
import com.dynamiccarsharing.carsharing.repository.filter.UserFilter;
import com.dynamiccarsharing.carsharing.util.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User save(User user) {
        Validator.validateNonNull(user, "User");
        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        Validator.validateId(id, "User ID");
        return userRepository.findById(id);
    }

    public void deleteById(Long id) {
        Validator.validateId(id, "User ID");
        userRepository.deleteById(id);
    }

    public Iterable<User> findAll() {
        return userRepository.findAll();
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + id + " not found"));
    }

    public User suspend(Long id) {
        User user = getUserOrThrow(id);
        validateUserStatus(user.getStatus(), UserStatus.ACTIVE, "Only active users can be suspended");
        return userRepository.save(user.withStatus(UserStatus.SUSPENDED));
    }

    public User ban(Long id) {
        User user = getUserOrThrow(id);
        validateUserStatus(user.getStatus(), UserStatus.ACTIVE, "Only active users can be banned");
        return userRepository.save(user.withStatus(UserStatus.BANNED));
    }

    public User activate(Long id) {
        User user = getUserOrThrow(id);
        validateUserStatus(user.getStatus(), UserStatus.SUSPENDED, "Only suspended users can be activated");
        return userRepository.save(user.withStatus(UserStatus.ACTIVE));
    }

    public User addCar(Long userId, Car car) {
        Validator.validateNonNull(car, "Car");
        User user = getUserOrThrow(userId);
        List<Car> updatedCars = new ArrayList<>(user.getCars());
        updatedCars.add(car);
        return userRepository.save(user.withCars(updatedCars));
    }

    public User removeCar(Long userId, Car car) {
        Validator.validateNonNull(car, "Car");
        User user = getUserOrThrow(userId);
        List<Car> updatedCars = new ArrayList<>(user.getCars());
        if (!updatedCars.remove(car)) {
            throw new IllegalArgumentException("Car not found in user's list");
        }
        return userRepository.save(user.withCars(updatedCars));
    }

    public User updateContactInfo(Long userId, ContactInfo newContactInfo) {
        Validator.validateNonNull(newContactInfo, "Contact information");
        User user = getUserOrThrow(userId);
        return userRepository.save(user.withContactInfo(newContactInfo));
    }

    public User signUp(String email, String password, ContactInfo contactInfo, UserRole role) {
        Validator.validateEmail(email, "Email");
        Validator.validateNonEmptyString(password, "Password");
        Validator.validateNonNull(contactInfo, "Contact information");
        Validator.validateNonNull(role, "Role");

        User newUser = new User(null, contactInfo, role, UserStatus.ACTIVE, new ArrayList<>());
        return userRepository.save(newUser);
    }

    private void validateUserStatus(UserStatus currentStatus, UserStatus expectedStatus, String errorMessage) {
        if (currentStatus != expectedStatus) {
            throw new IllegalStateException(errorMessage);
        }
    }

    public List<User> findUsersByRole(UserRole role) {
        Validator.validateNonNull(role, "User role");
        UserFilter filter = new UserFilter().setRole(role);
        return userRepository.findByFilter(filter);
    }

    public List<User> findUsersByStatus(UserStatus status) {
        Validator.validateNonNull(status, "User status");
        UserFilter filter = new UserFilter().setStatus(status);
        return userRepository.findByFilter(filter);
    }

    public List<User> findUsersByEmail(String email) {
        Validator.validateEmail(email, "User email");
        UserFilter filter = new UserFilter().setEmail(email);
        return userRepository.findByFilter(filter);
    }
}