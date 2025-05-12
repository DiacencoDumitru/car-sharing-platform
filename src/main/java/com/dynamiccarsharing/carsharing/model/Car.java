package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.util.Validator;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class Car {
    private final Long id;
    private final String registrationNumber;
    private final String make;
    private final String model;
    private final CarStatus status; // available, rented, maintenance
    private final Location location; // "New York"
    private final double price; // price per day
    private final CarType type; //"sedan", "SUV"
    private final VerificationStatus verificationStatus; // "pending", "verified", "rejected"

    public Car(Long id, String registrationNumber, String make, String model, CarStatus status, Location location, double price, CarType type, VerificationStatus verificationStatus) {
        Validator.validateId(id, "ID");
        Validator.validateNonEmptyString(registrationNumber, "Registration number");
        Validator.validateNonEmptyString(make, "Make");
        Validator.validateNonEmptyString(model, "Model");
        Validator.validateNonNull(status, "Status");
        Validator.validateNonNull(location, "Location");
        Validator.validateNonNegativeDouble(price, "Price");
        Validator.validateNonNull(type, "Type");
        Validator.validateNonNull(verificationStatus, "Verification status");
        this.id = id;
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.status = status;
        this.location = location;
        this.price = price;
        this.type = type;
        this.verificationStatus = verificationStatus;
    }

    public Car withCarStatus(CarStatus status) {
        return new Car(this.id, this.registrationNumber, this.make, this.model, status, this.location, this.price, this.type, this.verificationStatus);
    }

    public Car withVerificationStatus(VerificationStatus verificationStatus) {
        return new Car(this.id, this.registrationNumber, this.make, this.model, this.status, this.location, this.price, this.type, verificationStatus);
    }

    public Car withPrice(double price) {
        return new Car(this.id, this.registrationNumber, this.make, this.model, this.status, this.location, price, this.type, this.verificationStatus);
    }
}