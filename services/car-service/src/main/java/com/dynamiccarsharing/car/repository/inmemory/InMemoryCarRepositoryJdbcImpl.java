package com.dynamiccarsharing.car.repository.inmemory;

import com.dynamiccarsharing.car.criteria.CarSearchCriteria;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.repository.CarRepository;
import com.dynamiccarsharing.util.filter.Filter;
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
        List<Car> filteredCars = carMap.values().stream()
                .filter(car -> criteria.getMake() == null || car.getMake().equals(criteria.getMake()))
                .filter(car -> criteria.getModel() == null || car.getModel().equals(criteria.getModel()))
                .filter(car -> criteria.getOwnerId() == null || car.getOwnerId().equals(criteria.getOwnerId()))
                .filter(car -> criteria.getStatusIn() == null || criteria.getStatusIn().isEmpty() || criteria.getStatusIn().contains(car.getStatus()))
                .filter(car -> criteria.getLocationId() == null || car.getLocation().getId().equals(criteria.getLocationId()))
                .filter(car -> criteria.getType() == null || car.getType().equals(criteria.getType()))
                .filter(car -> criteria.getVerificationStatus() == null || car.getVerificationStatus().equals(criteria.getVerificationStatus()))
                .filter(car -> criteria.getMinAverageRating() == null
                        || (car.getAverageRating() != null
                        && car.getAverageRating().compareTo(criteria.getMinAverageRating()) >= 0))
                .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredCars.size());

        List<Car> pageContent = (start <= end) ? filteredCars.subList(start, end) : Collections.emptyList();

        return new PageImpl<>(pageContent, pageable, filteredCars.size());
    }

    public List<Car> findByStatus(CarStatus status) {
        return carMap.values().stream()
                .filter(car -> car.getStatus() == status)
                .toList();
    }
}