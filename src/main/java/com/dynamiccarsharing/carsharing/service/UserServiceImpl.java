package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserDto;
import com.dynamiccarsharing.carsharing.dto.UserStatusUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.criteria.UserSearchCriteria;
import com.dynamiccarsharing.carsharing.exception.CarNotFoundException;
import com.dynamiccarsharing.carsharing.exception.ServiceException;
import com.dynamiccarsharing.carsharing.exception.UserNotFoundException;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.UserFilter;
import com.dynamiccarsharing.carsharing.mapper.ContactInfoMapper;
import com.dynamiccarsharing.carsharing.mapper.UserMapper;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.CarRepository;
import com.dynamiccarsharing.carsharing.repository.UserRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("userService")
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final UserMapper userMapper;
    private final ContactInfoMapper contactInfoMapper;

    @Override
    public UserDto registerUser(UserCreateRequestDto createDto) {
        User user = userMapper.toEntity(createDto);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findUserById(Long id) {
        return userRepository.findById(id).map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toDto).toList();
    }

    @Override
    public void deleteById(Long id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new UserNotFoundException("User with ID " + id + " not found.");
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDto updateUserContactInfo(Long userId, ContactInfoUpdateRequestDto updateDto) {
        User userToUpdate = getUserOrThrow(userId);

        ContactInfo contactInfoToUpdate = userToUpdate.getContactInfo();

        contactInfoMapper.updateFromDto(updateDto, contactInfoToUpdate);

        User savedUser = userRepository.save(userToUpdate);

        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDto updateUserStatus(Long userId, UserStatusUpdateRequestDto updateDto) {
        User user = getUserOrThrow(userId);
        User updatedUser = userRepository.save(user.withStatus(updateDto.getStatus()));
        return userMapper.toDto(updatedUser);
    }

    @Override
    public void assignCarToUser(Long userId, Long carId) {
        User user = getUserOrThrow(userId);
        Car car = carRepository.findById(carId).orElseThrow(() -> new CarNotFoundException("Car with ID " + carId + " not found."));

        user.getCars().add(car);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> searchUsers(UserSearchCriteria criteria) {
        Filter<User> filter = UserFilter.of(criteria.getRole(), criteria.getStatus(), criteria.getEmail());
        try {
            return userRepository.findByFilter(filter);
        } catch (SQLException e) {
            throw new ServiceException("Search for users failed", e);
        }
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found."));
    }
}