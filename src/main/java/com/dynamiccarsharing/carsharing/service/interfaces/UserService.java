package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.dto.UserSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User registerUser(User user);

    Optional<User> findById(Long id);

    User updateUserStatus(Long userId, com.dynamiccarsharing.carsharing.enums.UserStatus newStatus);

    void assignCarToUser(Long userId, Long carId);

    List<User> searchUsers(UserSearchCriteria criteria);
}