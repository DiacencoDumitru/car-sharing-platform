package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.InMemoryUserRepository;
import com.dynamiccarsharing.carsharing.repository.filter.UserFilter;
import com.dynamiccarsharing.carsharing.util.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class UserService {

    private final InMemoryUserRepository inMemoryUserRepository;

    public UserService(InMemoryUserRepository inMemoryUserRepository) {
        this.inMemoryUserRepository = inMemoryUserRepository;
    }

    public User save(User user) {
        Validator.validateNonNull(user, "User");
        return inMemoryUserRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        Validator.validateId(id, "User ID");
        return inMemoryUserRepository.findById(id);
    }

    public void delete(Long id) {
        Validator.validateId(id, "User ID");
        inMemoryUserRepository.deleteById(id);
    }

    public Iterable<User> findAll() {
        return inMemoryUserRepository.findAll();
    }

    private User getUserOrThrow(Long id) {
        return inMemoryUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + id + " not found"));
    }

    public User suspend(Long id) {
        User user = getUserOrThrow(id);
        validateUserStatus(user.getStatus(), UserStatus.ACTIVE, "Only active users can be suspended");
        return inMemoryUserRepository.save(user.withStatus(UserStatus.SUSPENDED));
    }

    public User ban(Long id) {
        User user = getUserOrThrow(id);
        validateUserStatus(user.getStatus(), UserStatus.ACTIVE, "Only active users can be banned");
        return inMemoryUserRepository.save(user.withStatus(UserStatus.BANNED));
    }

    public User activate(Long id) {
        User user = getUserOrThrow(id);
        validateUserStatus(user.getStatus(), UserStatus.SUSPENDED, "Only suspended users can be activated");
        return inMemoryUserRepository.save(user.withStatus(UserStatus.ACTIVE));
    }

    public User addCar(Long userId, Car car) {
        Validator.validateNonNull(car, "Car");
        User user = getUserOrThrow(userId);
        List<Car> updatedCars = new ArrayList<>(user.getCars());
        updatedCars.add(car);
        return inMemoryUserRepository.save(user.withCars(updatedCars));
    }

    public User removeCar(Long userId, Car car) {
        Validator.validateNonNull(car, "Car");
        User user = getUserOrThrow(userId);
        List<Car> updatedCars = new ArrayList<>(user.getCars());
        if (!updatedCars.remove(car)) {
            throw new IllegalArgumentException("Car not found in user's list");
        }
        return inMemoryUserRepository.save(user.withCars(updatedCars));
    }

    public User updateContactInfo(Long userId, ContactInfo newContactInfo) {
        Validator.validateNonNull(newContactInfo, "Contact information");
        User user = getUserOrThrow(userId);
        return inMemoryUserRepository.save(user.withContactInfo(newContactInfo));
    }

    public User signUp(String email, String password, ContactInfo contactInfo, UserRole role) {
        Validator.validateEmail(email, "Email");
        Validator.validateNonEmptyString(password, "Password");
        Validator.validateNonNull(contactInfo, "Contact information");
        Validator.validateNonNull(role, "Role");

        User newUser = new User(
                null,
                contactInfo,
                role,
                UserStatus.ACTIVE,
                new ArrayList<>()
        );
        return inMemoryUserRepository.save(newUser);
    }

    private void validateUserStatus(UserStatus currentStatus, UserStatus expectedStatus, String errorMessage) {
        if (currentStatus != expectedStatus) {
            throw new IllegalStateException(errorMessage);
        }
    }

    public List<User> findUsersByRole(UserRole role) {
        Validator.validateNonNull(role, "User Role");
        UserFilter filter = new UserFilter().setRole(UserRole.ADMIN);
        return (List<User>) inMemoryUserRepository.findByFilter(filter);
    }
}