package com.dynamiccarsharing.user.service;

import com.dynamiccarsharing.contracts.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.contracts.dto.UserCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.contracts.dto.UserStatusUpdateRequestDto;
import com.dynamiccarsharing.user.criteria.UserSearchCriteria;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.user.exception.UserNotFoundException;
import com.dynamiccarsharing.util.filter.Filter;
import com.dynamiccarsharing.user.filter.UserFilter;
import com.dynamiccarsharing.user.mapper.ContactInfoMapper;
import com.dynamiccarsharing.user.mapper.UserMapper;
import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.user.repository.UserRepository;
import com.dynamiccarsharing.user.service.interfaces.UserService;
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
        user.setStatus(updateDto.getStatus());
        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
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