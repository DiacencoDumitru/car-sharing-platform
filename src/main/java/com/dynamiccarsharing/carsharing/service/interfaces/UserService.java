package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserDto;
import com.dynamiccarsharing.carsharing.dto.UserStatusUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.criteria.UserSearchCriteria;
import com.dynamiccarsharing.carsharing.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDto registerUser(UserCreateRequestDto createDto);

    Optional<UserDto> findUserById(Long id);

    List<UserDto> findAllUsers();

    void deleteById(Long id);

    UserDto updateUserStatus(Long userId, UserStatusUpdateRequestDto updateDto);

    UserDto updateUserContactInfo(Long userId, ContactInfoUpdateRequestDto updateDto);

    void assignCarToUser(Long userId, Long carId);

    List<User> searchUsers(UserSearchCriteria criteria);
}