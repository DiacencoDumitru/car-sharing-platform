package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryCarRepository implements CarRepository {
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
    public Iterable<Car> findAll() {
        return carMap.values();
    }

    @Override
    public Iterable<Car> findByFilter(Filter<Car> filter) {
        return carMap.values().stream().filter(filter::test).collect(Collectors.toList());
    }
}
