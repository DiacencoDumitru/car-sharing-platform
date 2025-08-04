package com.dynamiccarsharing.carsharing.service.interfaces;

<<<<<<< HEAD
import com.dynamiccarsharing.carsharing.dto.ContactInfoUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.UserDto;
import com.dynamiccarsharing.carsharing.dto.UserStatusUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.criteria.UserSearchCriteria;
import com.dynamiccarsharing.carsharing.model.User;
=======
import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.dto.criteria.UserSearchCriteria;
>>>>>>> fix/controller-mvc-tests

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDto registerUser(UserCreateRequestDto createDto);

    Optional<UserDto> findUserById(Long id);

<<<<<<< HEAD
    List<UserDto> findAllUsers();

    void deleteById(Long id);

    UserDto updateUserStatus(Long userId, UserStatusUpdateRequestDto updateDto);

    UserDto updateUserContactInfo(Long userId, ContactInfoUpdateRequestDto updateDto);
=======
    List<User> findAll();

    void deleteById(Long id);

    User updateUserStatus(Long userId, com.dynamiccarsharing.carsharing.enums.UserStatus newStatus);
>>>>>>> fix/controller-mvc-tests

    User updateUserContactInfo(Long userId, ContactInfo contactInfo);

    void assignCarToUser(Long userId, Long carId);

    List<User> searchUsers(UserSearchCriteria criteria);
}