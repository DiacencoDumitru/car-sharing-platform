package com.dynamiccarsharing.carsharing.repository.filter;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.CarType;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import lombok.Getter;

@Getter
public class CarFilter implements Filter<Car> {
    private final String make;
    private final String model;
    private final CarStatus status;
    private final Location location;
    private final CarType type;
    private final VerificationStatus verificationStatus;

    private CarFilter(String make, String model, CarStatus status, Location location, CarType type, VerificationStatus verificationStatus) {
        this.make = make;
        this.model = model;
        this.status = status;
        this.location = location;
        this.type = type;
        this.verificationStatus = verificationStatus;
    }

    public static CarFilter of(String make, String model, CarStatus status, Location location, CarType type, VerificationStatus verificationStatus) {
        return new CarFilter(make, model, status, location, type, verificationStatus);
    }

    public static CarFilter ofMake(String make) {
        return new CarFilter(make, null, null, null, null, null);
    }

    public static CarFilter ofModel(String model) {
        return new CarFilter(null, model, null, null, null, null);
    }

    public static CarFilter ofStatus(CarStatus status) {
        return new CarFilter(null, null, status, null, null, null);
    }

    public static CarFilter ofLocation(Location location) {
        return new CarFilter(null, null, null, location, null, null);
    }

    public static CarFilter ofType(CarType type) {
        return new CarFilter(null, null, null, null, type, null);
    }

    public static CarFilter ofVerificationStatus(VerificationStatus verificationStatus) {
        return new CarFilter(null, null, null, null, null, verificationStatus);
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
