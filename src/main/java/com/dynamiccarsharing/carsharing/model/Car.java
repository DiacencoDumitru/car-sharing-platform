package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.util.Validator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.With;

@Getter
@ToString
@EqualsAndHashCode
public class Car {
    private final Long id;
    private final String registrationNumber;
    private final String make;
    private final String model;
    @With
    private final CarStatus status;
    private final Location location;
    @With
    private final double price;
    private final CarType type;
    @With
    private final VerificationStatus verificationStatus;

    public Car(Long id, String registrationNumber, String make, String model, CarStatus status, Location location, double price, CarType type, VerificationStatus verificationStatus) {

        if (id != null) {
            Validator.validateId(id, "ID");
        }

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
}