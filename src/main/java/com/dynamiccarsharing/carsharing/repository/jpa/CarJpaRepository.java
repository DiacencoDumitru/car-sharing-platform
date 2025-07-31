package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.filter.CarFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.repository.CarRepository;
import com.dynamiccarsharing.carsharing.specification.CarSpecification;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Profile("jpa")
@Repository
public interface CarJpaRepository extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car>, CarRepository {
    Optional<Car> findByRegistrationNumber(String registrationNumber);

    List<Car> findByMakeAndModel(String make, String model);

    List<Car> findByStatus(CarStatus status);

    @Override
    default List<Car> findByFilter(Filter<Car> filter) throws SQLException {
        if (!(filter instanceof CarFilter carFilter)) {
            throw new IllegalArgumentException("Filter must be an instance of CarFilter for JPA search.");
        }
        return findAll(CarSpecification.withCriteria(
                carFilter.getMake(),
                carFilter.getModel(),
                carFilter.getStatus(),
                carFilter.getLocation().getId(),
                carFilter.getType(),
                carFilter.getVerificationStatus()
        ));
    }
}