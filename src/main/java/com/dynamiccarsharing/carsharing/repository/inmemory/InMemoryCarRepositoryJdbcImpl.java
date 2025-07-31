package com.dynamiccarsharing.carsharing.repository.inmemory;

import com.dynamiccarsharing.carsharing.enums.CarStatus;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.repository.CarRepository;
import com.dynamiccarsharing.carsharing.filter.Filter;

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
    public Iterable<Car> findAll() {
        return carMap.values();
    }

    public List<Car> findByStatus(CarStatus status) {
        return carMap.values().stream()
                .filter(car -> car.getStatus() == status)
                .toList();
    }
}