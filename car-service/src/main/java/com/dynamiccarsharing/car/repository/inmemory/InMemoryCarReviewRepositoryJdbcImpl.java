package com.dynamiccarsharing.car.repository.inmemory;

import com.dynamiccarsharing.car.model.CarReview;
import com.dynamiccarsharing.car.repository.CarReviewRepository;
import com.dynamiccarsharing.util.filter.Filter;

import java.util.*;

public class InMemoryCarReviewRepositoryJdbcImpl implements CarReviewRepository {
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
        return carReviewMap.values().stream().filter(filter::test).toList();
    }

    @Override
    public List<CarReview> findByCarId(Long carId) {
        return carReviewMap.values().stream()
                .filter(review -> review.getCar() != null && review.getCar().getId().equals(carId))
                .toList();
    }

    @Override
    public List<CarReview> findByReviewerId(Long reviewerId) {
        return carReviewMap.values().stream()
                .filter(review -> review.getReviewerId().equals(reviewerId))
                .toList();
    }

    @Override
    public List<CarReview> findAll() {
        return new ArrayList<>(carReviewMap.values());
    }
}