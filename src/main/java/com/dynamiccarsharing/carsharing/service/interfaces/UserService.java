package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.model.ContactInfo;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.dto.criteria.UserSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(User user);

    Optional<User> findById(Long id);

    List<User> findAll();

    void deleteById(Long id);

    User updateUserStatus(Long userId, com.dynamiccarsharing.carsharing.enums.UserStatus newStatus);

    User updateUserContactInfo(Long userId, ContactInfo contactInfo);

    void assignCarToUser(Long userId, Long carId);

    List<User> searchUsers(UserSearchCriteria criteria);
}