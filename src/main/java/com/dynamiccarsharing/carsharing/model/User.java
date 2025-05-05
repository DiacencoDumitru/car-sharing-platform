package com.dynamiccarsharing.carsharing.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class User {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String role; // "Renter", "CarOwner", "Admin", "Guest"
    private String status; // "active", "suspended", "banned"
    private List<Car> carList;

    public User() {
        this.carList = new ArrayList<>();
    }

    public User(Long id, String name, String email, String phoneNumber, String role, String status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.status = status;
        this.carList = new ArrayList<>();
    }
}
