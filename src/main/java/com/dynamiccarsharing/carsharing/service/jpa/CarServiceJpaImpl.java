package com.dynamiccarsharing.carsharing.service.jpa;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.exception.CarNotFoundException;
import com.dynamiccarsharing.carsharing.exception.InvalidCarStatusException;
import com.dynamiccarsharing.carsharing.exception.InvalidVerificationStatusException;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.repository.jpa.CarJpaRepository;
import com.dynamiccarsharing.carsharing.specification.CarSpecification;
import com.dynamiccarsharing.carsharing.service.interfaces.CarService;
import com.dynamiccarsharing.carsharing.dto.CarSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service("carService")
@Profile("jpa")
@Transactional
public class CarServiceJpaImpl implements CarService {

    private final CarJpaRepository carRepository;

    public CarServiceJpaImpl(CarJpaRepository carRepository) {
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
        if (!carRepository.existsById(id)) {
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
        if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("New Price must not be negative.");
        }
        Car car = getCarOrThrow(carId);
        return carRepository.save(car.withPrice(newPrice));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Car> searchCars(CarSearchCriteria criteria) {
        Specification<Car> spec = CarSpecification.withCriteria(
                criteria.getMake(),
                criteria.getModel(),
                criteria.getStatus(),
                criteria.getLocationId(),
                criteria.getType(),
                criteria.getVerificationStatus()
        );
        return carRepository.findAll(spec);
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