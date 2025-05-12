package com.dynamiccarsharing.carsharing.repository.filter;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;

public class CarFilter implements Filter<Car> {
    private String make;
    private String model;
    private CarStatus status;
    private Location location;
    private CarType type;
    private VerificationStatus verificationStatus;

    public CarFilter setMake(String make) {
        this.make = make;
        return this;
    }

    public CarFilter setModel(String model) {
        this.model = model;
        return this;
    }

    public CarFilter setStatus(CarStatus status) {
        this.status = status;
        return this;
    }

    public CarFilter setLocation(Location location) {
        this.location = location;
        return this;
    }

    public CarFilter setType(CarType type) {
        this.type = type;
        return this;
    }

    public CarFilter setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
        return this;
    }

    @Override
    public boolean test(Car car) {
        boolean matches = true;
        if (make != null) matches &= car.getMake().equals(make);
        if (model != null) matches &= car.getModel().equals(model);
        if (status != null) matches &= car.getStatus() == status;
        if (location != null) matches &= car.getLocation().equals(location);
        if (type != null) matches &= car.getType() == type;
        if (verificationStatus != null) matches &= car.getVerificationStatus() == verificationStatus;
        return matches;
    }
}
