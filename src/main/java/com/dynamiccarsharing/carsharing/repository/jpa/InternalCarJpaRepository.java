package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("jpa")
interface InternalCarJpaRepository extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car> {
    Optional<Car> findByRegistrationNumber(String registrationNumber);

    List<Car> findByMakeAndModel(String make, String model);

    List<Car> findByStatus(CarStatus status);
}