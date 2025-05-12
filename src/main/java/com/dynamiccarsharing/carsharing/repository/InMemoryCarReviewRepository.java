package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryCarReviewRepository implements CarReviewRepository {
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
    public Iterable<CarReview> findAll() {
        return carReviewMap.values();
    }

    @Override
    public Iterable<CarReview> findByFilter(Filter<CarReview> filter) {
        return carReviewMap.values().stream().filter(filter::test).collect(Collectors.toList());
    }
}
