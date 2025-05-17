package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.util.Validator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.With;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@EqualsAndHashCode
public class User {
    private final Long id;
    @With
    private final ContactInfo contactInfo;
    @With
    private final UserRole role;
    @With
    private final UserStatus status;
    @With
    private final List<Car> cars;

    public User(Long id, ContactInfo contactInfo, UserRole role, UserStatus status, List<Car> cars) {
        Validator.validateNonNull(contactInfo, "Contact info");
        Validator.validateNonNull(role, "Role");
        Validator.validateNonNull(status, "Status");
        Validator.validateNonNullList(cars, "Cars list");
        this.id = id;
        this.contactInfo = contactInfo;
        this.role = role;
        this.status = status;
        this.cars = new ArrayList<>(cars);
    }

    public User(Long id, ContactInfo contactInfo, UserRole role, UserStatus status) {
        this(id, contactInfo, role, status, new ArrayList<>());
    }

    public List<Car> getCars() {
        return new ArrayList<>(cars);
    }
}
