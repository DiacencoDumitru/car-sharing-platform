package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.dto.criteria.CarSearchCriteria;
import com.dynamiccarsharing.carsharing.exception.ValidationException;
import com.dynamiccarsharing.carsharing.filter.CarFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.repository.CarRepository;
import com.dynamiccarsharing.carsharing.specification.CarSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Profile("jpa")
public class CarJpaRepositoryImpl implements CarRepository {

    private final InternalCarJpaRepository internalCarJpaRepository;

    @Override
    public List<Car> findByFilter(Filter<Car> filter) throws SQLException {
        if (!(filter instanceof CarFilter carFilter)) {
            throw new ValidationException("Filter must be an instance of CarFilter for JPA search.");
        }
        return internalCarJpaRepository.findAll(CarSpecification.withCriteria(
                carFilter.getMake(),
                carFilter.getModel(),
                carFilter.getStatus() != null ? List.of(carFilter.getStatus()) : Collections.emptyList(),
                carFilter.getLocation() != null ? carFilter.getLocation().getId() : null,
                carFilter.getType(),
                null,
                null,
                carFilter.getVerificationStatus()
        ));
    }

    @Override
    public Car save(Car entity) {
        return internalCarJpaRepository.save(entity);
    }

    @Override
    public Optional<Car> findById(Long id) {
        return internalCarJpaRepository.findById(id);
    }

    @Override
    public List<Car> findAll() {
        return internalCarJpaRepository.findAll();
    }

    @Override
    public Page<Car> findAll(CarSearchCriteria criteria, Pageable pageable) {
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
        return internalCarJpaRepository.findAll(spec, pageable);
    }

    @Override
    public void deleteById(Long id) {
        internalCarJpaRepository.deleteById(id);
    }
}