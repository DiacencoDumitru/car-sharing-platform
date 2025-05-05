package com.dynamiccarsharing.carsharing.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class Car {
    private Long id;
    private String registrationNumber;
    private String model;
    private String status;
    private String location; // "New York"
    private double price; // price per day
    private String type; //"sedan", "SUV"
    private String verificationStatus; // "pending", "verified"
}