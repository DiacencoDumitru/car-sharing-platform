package com.dynamiccarsharing.carsharing.repository.inmemory;

import com.dynamiccarsharing.carsharing.dto.criteria.CarSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.filter.CarFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.CarRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

public class InMemoryCarRepositoryJdbcImpl implements CarRepository {
    private final Map<Long, Car> carMap = new HashMap<>();

    @Override
    public Car save(Car car) {
        carMap.put(car.getId(), car);
        return car;
    }

    @Override
    public Optional<Car> findById(Long id) {
        return Optional.ofNullable(carMap.get(id));
    }

    @Override
    public void deleteById(Long id) {
        carMap.remove(id);
    }

    @Override
    public List<Car> findByFilter(Filter<Car> filter) {
        return carMap.values().stream().filter(filter::test).toList();
    }

    @Override
    public List<Car> findAll() {
        return new ArrayList<>(carMap.values());
    }

    @Override
    public Page<Car> findAll(CarSearchCriteria criteria, Pageable pageable) {
        Filter<Car> filter = CarFilter.of(criteria.getMake(), criteria.getModel(),
                criteria.getStatusIn() != null && !criteria.getStatusIn().isEmpty() ? criteria.getStatusIn().get(0) : null,
                criteria.getLocationId() != null ? Location.builder().id(criteria.getLocationId()).build() : null,
                criteria.getType(), criteria.getVerificationStatus());

        List<Car> filteredCars = carMap.values().stream().filter(filter::test).toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredCars.size());

        List<Car> pageContent = (start <= end) ? filteredCars.subList(start, end) : List.of();

        return new PageImpl<>(pageContent, pageable, filteredCars.size());
    }

    public List<Car> findByStatus(CarStatus status) {
        return carMap.values().stream()
                .filter(car -> car.getStatus() == status)
                .toList();
    }
}