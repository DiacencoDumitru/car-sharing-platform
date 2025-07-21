package com.dynamiccarsharing.carsharing.service.jdbc;

import com.dynamiccarsharing.carsharing.exception.CarNotFoundException;
import com.dynamiccarsharing.carsharing.exception.UserNotFoundException;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.UserFilter;
import com.dynamiccarsharing.carsharing.repository.jdbc.CarRepositoryJdbcImpl;
import com.dynamiccarsharing.carsharing.repository.jdbc.UserRepositoryJdbcImpl;
import com.dynamiccarsharing.carsharing.service.interfaces.UserService;
import com.dynamiccarsharing.carsharing.dto.UserSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("userService")
@Profile("jdbc")
@Transactional
public class UserServiceJdbcImpl implements UserService {

    private final UserRepositoryJdbcImpl userRepositoryJdbcImpl;
    private final CarRepositoryJdbcImpl carRepositoryJdbcImpl;

    public UserServiceJdbcImpl(UserRepositoryJdbcImpl userRepositoryJdbcImpl, CarRepositoryJdbcImpl carRepositoryJdbcImpl) {
        this.userRepositoryJdbcImpl = userRepositoryJdbcImpl;
        this.carRepositoryJdbcImpl = carRepositoryJdbcImpl;
    }

    @Override
    @Transactional
    public User registerUser(User user) {
        return userRepositoryJdbcImpl.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepositoryJdbcImpl.findById(id);
    }

    @Override
    @Transactional
    public User updateUserStatus(Long userId, com.dynamiccarsharing.carsharing.enums.UserStatus newStatus) {
        User user = userRepositoryJdbcImpl.findById(userId).orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found."));
        User updatedUser = user.withStatus(newStatus);
        return userRepositoryJdbcImpl.save(updatedUser);
    }

    @Override
    @Transactional
    public void assignCarToUser(Long userId, Long carId) {
        User user = userRepositoryJdbcImpl.findById(userId).orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found."));
        Car car = carRepositoryJdbcImpl.findById(carId).orElseThrow(() -> new CarNotFoundException("Car with ID " + carId + " not found."));

        user.getCars().add(car);
        userRepositoryJdbcImpl.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> searchUsers(UserSearchCriteria criteria) {
        Filter<User> filter = createFilterFromCriteria(criteria);
        try {
            return userRepositoryJdbcImpl.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search for users failed", e);
        }
    }

    private Filter<User> createFilterFromCriteria(UserSearchCriteria criteria) {
        return UserFilter.of(
                criteria.getRole(),
                criteria.getStatus(),
                criteria.getEmail()
        );
    }
}