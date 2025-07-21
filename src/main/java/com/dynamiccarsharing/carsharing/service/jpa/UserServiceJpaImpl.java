package com.dynamiccarsharing.carsharing.service.jpa;

import com.dynamiccarsharing.carsharing.exception.CarNotFoundException;
import com.dynamiccarsharing.carsharing.exception.UserNotFoundException;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.jpa.CarJpaRepository;
import com.dynamiccarsharing.carsharing.repository.jpa.UserJpaRepository;
import com.dynamiccarsharing.carsharing.specification.UserSpecification;
import com.dynamiccarsharing.carsharing.service.interfaces.UserService;
import com.dynamiccarsharing.carsharing.dto.UserSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("userService")
@Profile("jpa")
@Transactional
public class UserServiceJpaImpl implements UserService {

    private final UserJpaRepository userRepository;
    private final CarJpaRepository carRepository;

    public UserServiceJpaImpl(UserJpaRepository userRepository, CarJpaRepository carRepository) {
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
    public User updateUserStatus(Long userId, com.dynamiccarsharing.carsharing.enums.UserStatus newStatus) {
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
        return userRepository.findAll(
                UserSpecification.withCriteria(
                        criteria.getEmail(),
                        criteria.getRole(),
                        criteria.getStatus()
                )
        );
    }
}