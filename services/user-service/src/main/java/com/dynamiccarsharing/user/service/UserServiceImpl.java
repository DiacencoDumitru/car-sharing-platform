package com.dynamiccarsharing.user.service;

import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.criteria.UserSearchCriteria;
import com.dynamiccarsharing.user.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.user.dto.UserCreateRequestDto;
import com.dynamiccarsharing.user.dto.UserStatusUpdateRequestDto;
import com.dynamiccarsharing.user.exception.UserNotFoundException;
import com.dynamiccarsharing.user.filter.UserFilter;
import com.dynamiccarsharing.user.mapper.ContactInfoMapper;
import com.dynamiccarsharing.user.mapper.UserMapper;
import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.user.referral.ReferralCodeAllocator;
import com.dynamiccarsharing.user.repository.UserRepository;
import com.dynamiccarsharing.user.service.interfaces.UserService;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.filter.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("userService")
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ContactInfoMapper contactInfoMapper;
    private final ReferralCodeAllocator referralCodeAllocator;

    @Override
    public UserDto registerUser(UserCreateRequestDto createDto) {
        User user = userMapper.toEntity(createDto);
        user.setStatus(UserStatus.ACTIVE);
        user.setReferralCode(referralCodeAllocator.allocate());
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
    public Optional<UserDto> findByEmail(String email) {
        return userRepository.findByContactInfoEmail(email)
                .map(userMapper::toDto);
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByContactInfoEmail(username)
                .orElse(null);

        if (user == null) {
            try {
                Long userId = Long.parseLong(username);
                user = userRepository.findById(userId).orElse(null);
            } catch (NumberFormatException ignored) {
            }
        }

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return user;
    }
}