package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.criteria.CarSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.exception.CarNotFoundException;
import com.dynamiccarsharing.carsharing.exception.InvalidCarStatusException;
import com.dynamiccarsharing.carsharing.exception.InvalidVerificationStatusException;
import com.dynamiccarsharing.carsharing.filter.CarFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.repository.CarRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.CarService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("carService")
@Transactional
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;

    public CarServiceImpl(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @Override
    public Car save(Car car) {
        return carRepository.save(car);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Car> findById(Long id) {
        return carRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        if (carRepository.findById(id).isEmpty()) {
            throw new CarNotFoundException("Car with ID " + id + " not found.");
        }
        carRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<Car> findAll() {
        return carRepository.findAll();
    }

    @Override
    public Car rentCar(Long carId) {
        Car car = getCarOrThrow(carId);
        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new InvalidCarStatusException("Car can only be rented if AVAILABLE");
        }
        return carRepository.save(car.withStatus(CarStatus.RENTED));
    }

    @Override
    public Car returnCar(Long carId) {
        Car car = getCarOrThrow(carId);
        validateCarStatus(car.getStatus(), CarStatus.RENTED, "Car can only be returned if RENTED");
        return carRepository.save(car.withStatus(CarStatus.AVAILABLE));
    }

    @Override
    public Car setMaintenance(Long carId) {
        Car car = getCarOrThrow(carId);
        validateCarStatus(car.getStatus(), CarStatus.AVAILABLE, "Cannot set MAINTENANCE for a RENTED car");
        return carRepository.save(car.withStatus(CarStatus.MAINTENANCE));
    }

    @Override
    public Car verifyCar(Long carId) {
        Car car = getCarOrThrow(carId);
        validateVerificationStatus(car.getVerificationStatus(), "Car can only be verified from PENDING status");
        return carRepository.save(car.withVerificationStatus(VerificationStatus.VERIFIED));
    }

    @Override
    public Car rejectVerification(Long carId) {
        Car car = getCarOrThrow(carId);
        validateVerificationStatus(car.getVerificationStatus(), "Car can only be rejected from PENDING status");
        return carRepository.save(car.withVerificationStatus(VerificationStatus.REJECTED));
    }

    @Override
    public Car updatePrice(Long carId, BigDecimal newPrice) {
        if (newPrice != null && newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("New Price must not be negative.");
        }
        Car car = getCarOrThrow(carId);
        return carRepository.save(car.withPrice(newPrice));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Car> searchCars(CarSearchCriteria criteria) {
        Filter<Car> filter = CarFilter.of(
                criteria.getMake(),
                criteria.getModel(),
                criteria.getStatus(),
                criteria.getLocation(),
                criteria.getType(),
                criteria.getVerificationStatus()
        );
        try {
            return carRepository.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search for cars failed due to a database error", e);
        }
    }

    private Car getCarOrThrow(Long carId) {
        return carRepository.findById(carId).orElseThrow(() -> new CarNotFoundException("Car with ID " + carId + " not found"));
    }

    private void validateCarStatus(CarStatus currentStatus, CarStatus expectedStatus, String message) {
        if (currentStatus != expectedStatus) {
            throw new InvalidCarStatusException(message);
        }
    }

    private void validateVerificationStatus(VerificationStatus currentStatus, String message) {
        if (currentStatus != VerificationStatus.PENDING) {
            throw new InvalidVerificationStatusException(message);
        }
    }
}