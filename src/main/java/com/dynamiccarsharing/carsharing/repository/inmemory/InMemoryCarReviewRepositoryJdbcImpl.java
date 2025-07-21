package com.dynamiccarsharing.carsharing.repository.inmemory;

import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.repository.jdbc.CarReviewRepositoryJdbcImpl;
import com.dynamiccarsharing.carsharing.filter.Filter;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryCarReviewRepositoryJdbcImpl implements CarReviewRepositoryJdbcImpl {
    private final Map<Long, CarReview> carReviewMap = new HashMap<>();

    @Override
    public CarReview save(CarReview carReview) {
        carReviewMap.put(carReview.getId(), carReview);
        return carReview;
    }

    @Override
    public Optional<CarReview> findById(Long id) {
        return Optional.ofNullable(carReviewMap.get(id));
    }

    @Override
    public void deleteById(Long id) {
        carReviewMap.remove(id);
    }

    @Override
    public List<CarReview> findByFilter(Filter<CarReview> filter) {
        return carReviewMap.values().stream().filter(filter::test).collect(Collectors.toList());
    }

    @Override
    public Iterable<CarReview> findAll() {
        return carReviewMap.values();
    }
}