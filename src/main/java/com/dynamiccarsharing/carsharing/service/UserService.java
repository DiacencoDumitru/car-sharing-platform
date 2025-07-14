package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.exception.InvalidUserStatusException;
import com.dynamiccarsharing.carsharing.exception.UserNotFoundException;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public void deleteById(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with ID " + id + " not found.");
        }
        userRepository.deleteById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User suspend(UUID id) {
        User user = getUserOrThrow(id);
        validateUserStatus(user.getStatus(), UserStatus.ACTIVE, "Only active users can be suspended");
        return userRepository.save(user.withStatus(UserStatus.SUSPENDED));
    }

    public User ban(UUID id) {
        User user = getUserOrThrow(id);
        validateUserStatus(user.getStatus(), UserStatus.ACTIVE, "Only active users can be banned");
        return userRepository.save(user.withStatus(UserStatus.BANNED));
    }

    public User activate(UUID id) {
        User user = getUserOrThrow(id);
        validateUserStatus(user.getStatus(), UserStatus.SUSPENDED, "Only suspended users can be activated");
        return userRepository.save(user.withStatus(UserStatus.ACTIVE));
    }

    public User addCarToUser(UUID userId, Car car) {
        User user = userRepository.findWithCarsById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));
        user.getCars().add(car);
        return user;
    }

    public User removeCarFromUser(UUID userId, Car car) {
        User user = userRepository.findWithCarsById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));
        user.getCars().remove(car);
        return user;
    }

    public User updateContactInfo(UUID userId, ContactInfo newContactInfo) {
        User user = getUserOrThrow(userId);
        return userRepository.save(user.withContactInfo(newContactInfo));
    }

    public User signUp(ContactInfo contactInfo, UserRole role) {
        User newUser = User.builder()
                .contactInfo(contactInfo)
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();
        return userRepository.save(newUser);
    }

    public List<User> findUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public List<User> findUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByContactInfoEmail(email);
    }

    private User getUserOrThrow(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));
    }

    private void validateUserStatus(UserStatus currentStatus, UserStatus expectedStatus, String errorMessage) {
        if (currentStatus != expectedStatus) {
            throw new InvalidUserStatusException(errorMessage);
        }
    }
}