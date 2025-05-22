package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.filter.UserFilter;

import java.util.List;

public interface UserRepository extends Repository<User, Long> {
    List<User> findByFilter(UserFilter filter);
}
