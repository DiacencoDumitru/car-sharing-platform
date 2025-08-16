package com.dynamiccarsharing.user.service.interfaces;


import com.dynamiccarsharing.user.criteria.UserSearchCriteria;
import com.dynamiccarsharing.contracts.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.contracts.dto.UserCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.contracts.dto.UserStatusUpdateRequestDto;
import com.dynamiccarsharing.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDto registerUser(UserCreateRequestDto createDto);

    Optional<UserDto> findUserById(Long id);

    List<UserDto> findAllUsers();

    void deleteById(Long id);

    UserDto updateUserStatus(Long userId, UserStatusUpdateRequestDto updateDto);

    UserDto updateUserContactInfo(Long userId, ContactInfoUpdateRequestDto updateDto);

    List<User> searchUsers(UserSearchCriteria criteria);
}