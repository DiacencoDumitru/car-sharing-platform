package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.enums.UserRole;
import com.dynamiccarsharing.carsharing.enums.UserStatus;
import com.dynamiccarsharing.carsharing.util.Validator;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
public class User {
    private final Long id;
    private final ContactInfo contactInfo;
    private final UserRole role; // "Renter", "CarOwner", "Admin", "Guest"
    private final UserStatus status; // "active", "suspended", "banned"
    private final List<Car> cars;

    public User(Long id, ContactInfo contactInfo, UserRole role, UserStatus status, List<Car> cars) {
        Validator.validateId(id, "ID");
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

    public User withContactInfo(ContactInfo contactInfo) {
        return new User(this.id, contactInfo, this.role, this.status, this.cars);
    }

    public User withRole(UserRole role) {
        return new User(this.id, this.contactInfo, role, this.status, this.cars);
    }

    public User withStatus(UserStatus status) {
        return new User(this.id, this.contactInfo, this.role, status, this.cars);
    }

    public User withCars(List<Car> cars) {
        return new User(this.id, this.contactInfo, this.role, this.status, cars);
    }
}
