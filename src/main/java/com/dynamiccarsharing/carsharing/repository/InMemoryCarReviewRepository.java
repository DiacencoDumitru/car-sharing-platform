package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.repository.filter.CarReviewFilter;

import java.util.*;

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
    public List<CarReview> findByFilter(CarReviewFilter filter) {
        return carReviewMap.values().stream().filter(filter::test).toList();
    }
}
