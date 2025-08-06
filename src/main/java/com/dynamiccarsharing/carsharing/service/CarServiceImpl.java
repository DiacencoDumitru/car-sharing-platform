package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.CarCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.CarDto;
import com.dynamiccarsharing.carsharing.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.criteria.CarSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.enums.VerificationStatus;
import com.dynamiccarsharing.carsharing.exception.CarNotFoundException;
import com.dynamiccarsharing.carsharing.exception.InvalidCarStatusException;
import com.dynamiccarsharing.carsharing.exception.InvalidVerificationStatusException;
import com.dynamiccarsharing.carsharing.exception.ValidationException;
import com.dynamiccarsharing.carsharing.mapper.CarMapper;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.repository.jpa.CarJpaRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.CarService;
import com.dynamiccarsharing.carsharing.specification.CarSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service("carService")
@Transactional
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarJpaRepository carRepository;
    private final CarMapper carMapper;

    @Override
    public CarDto save(CarCreateRequestDto carDto) {
        Car car = carMapper.toEntity(carDto);
        Car savedCar = carRepository.save(car);
        return carMapper.toDto(savedCar);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CarDto> findById(Long id) {
        return carRepository.findById(id).map(carMapper::toDto);
    }

    @Override
    public void deleteById(Long id) {
        if (carRepository.findById(id).isPresent()) {
            carRepository.deleteById(id);
        } else {
            throw new CarNotFoundException("Car with ID " + id + " not found.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CarDto> findAll(CarSearchCriteria criteria, Pageable pageable) {
        Specification<Car> spec = CarSpecification.withCriteria(
                criteria.getMake(),
                criteria.getModel(),
                criteria.getStatusIn(),
                criteria.getLocationId(),
                criteria.getType(),
                criteria.getPriceGreaterThan(),
                criteria.getPriceLessThan(),
                criteria.getVerificationStatus()
        );

        Page<Car> carPage = carRepository.findAll(spec, pageable);

        return carPage.map(carMapper::toDto);
    }

    @Override
    public CarDto rentCar(Long carId) {
        Car car = getCarOrThrow(carId);
        if (car.getStatus() != CarStatus.AVAILABLE) {
            throw new InvalidCarStatusException("Car can only be rented if AVAILABLE");
        }
        Car updatedCar = carRepository.save(car.withStatus(CarStatus.RENTED));
        return carMapper.toDto(updatedCar);
    }

    @Override
    public CarDto returnCar(Long carId) {
        Car car = getCarOrThrow(carId);
        validateCarStatus(car.getStatus(), CarStatus.RENTED, "Car can only be returned if RENTED");
        Car updatedCar = carRepository.save(car.withStatus(CarStatus.AVAILABLE));
        return carMapper.toDto(updatedCar);
    }

    @Override
    public CarDto setMaintenance(Long carId) {
        Car car = getCarOrThrow(carId);
        validateCarStatus(car.getStatus(), CarStatus.AVAILABLE, "Cannot set MAINTENANCE for a RENTED car");
        Car updatedCar = carRepository.save(car.withStatus(CarStatus.MAINTENANCE));
        return carMapper.toDto(updatedCar);
    }

    @Override
    public CarDto verifyCar(Long carId) {
        Car car = getCarOrThrow(carId);
        validateVerificationStatus(car.getVerificationStatus(), "Car can only be verified from PENDING status");
        Car updatedCar = carRepository.save(car.withVerificationStatus(VerificationStatus.VERIFIED));
        return carMapper.toDto(updatedCar);
    }

    @Override
    public CarDto rejectVerification(Long carId) {
        Car car = getCarOrThrow(carId);
        validateVerificationStatus(car.getVerificationStatus(), "Car can only be rejected from PENDING status");
        Car updatedCar = carRepository.save(car.withVerificationStatus(VerificationStatus.REJECTED));
        return carMapper.toDto(updatedCar);
    }

    @Override
    public CarDto updateCar(Long carId, CarUpdateRequestDto updateDto) {
        Car carToUpdate = getCarOrThrow(carId);

        carMapper.updateCarFromDto(updateDto, carToUpdate);

        Car updatedCar = carRepository.save(carToUpdate);
        return carMapper.toDto(updatedCar);
    }

    @Override
    public CarDto updatePrice(Long carId, BigDecimal newPrice) {
        if (newPrice != null && newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("New Price must not be negative.");
        }
        Car car = getCarOrThrow(carId);
        Car updatedCar = carRepository.save(car.withPrice(newPrice));
        return carMapper.toDto(updatedCar);
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