package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID>, JpaSpecificationExecutor<Car> {
    Optional<Car> findByRegistrationNumber(String registrationNumber);

    List<Car> findByMakeAndModel(String make, String model);

    List<Car> findByStatus(CarStatus status);
}
