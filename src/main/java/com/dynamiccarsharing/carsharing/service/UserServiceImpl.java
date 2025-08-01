package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.criteria.UserSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.exception.CarNotFoundException;
import com.dynamiccarsharing.carsharing.exception.UserNotFoundException;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.UserFilter;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.CarRepository;
import com.dynamiccarsharing.carsharing.repository.UserRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("userService")
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CarRepository carRepository;

    public UserServiceImpl(UserRepository userRepository, CarRepository carRepository) {
        this.userRepository = userRepository;
        this.carRepository = carRepository;
    }

    @Override
    public User registerUser(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        if (userRepository.findById(id).isPresent()) {
            throw new UserNotFoundException("User with ID " + id + " not found.");
        }
        userRepository.deleteById(id);
    }

    @Override
    public User updateUserContactInfo(Long userId, ContactInfo contactInfo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found."));

        user = user.withContactInfo(contactInfo);
        return userRepository.save(user);
    }

    @Override
    public User updateUserStatus(Long userId, UserStatus newStatus) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found."));
        return userRepository.save(user.withStatus(newStatus));
    }

    @Override
    public void assignCarToUser(Long userId, Long carId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found."));
        Car car = carRepository.findById(carId).orElseThrow(() -> new CarNotFoundException("Car with ID " + carId + " not found."));

        user.getCars().add(car);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> searchUsers(UserSearchCriteria criteria) {
        Filter<User> filter = UserFilter.of(
                criteria.getRole(),
                criteria.getStatus(),
                criteria.getEmail()
        );
        try {
            return userRepository.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search for users failed", e);
        }
    }
}