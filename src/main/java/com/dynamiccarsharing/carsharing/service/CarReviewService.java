package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.repository.InMemoryCarReviewRepository;
import com.dynamiccarsharing.carsharing.repository.filter.CarReviewFilter;
import com.dynamiccarsharing.carsharing.util.Validator;

import java.util.List;
import java.util.Optional;

public class CarReviewService {
    private final InMemoryCarReviewRepository inMemoryCarReviewRepository;

    public CarReviewService(InMemoryCarReviewRepository inMemoryCarReviewRepository) {
        this.inMemoryCarReviewRepository = inMemoryCarReviewRepository;
    }

    public CarReview save(CarReview carReview) {
        Validator.validateNonNull(carReview, "Car Review");
        return inMemoryCarReviewRepository.save(carReview);
    }

    public Optional<CarReview> findById(Long id) {
        Validator.validateId(id, "ID");
        return inMemoryCarReviewRepository.findById(id);
    }

    public void delete(Long id) {
        Validator.validateId(id, "ID");
        inMemoryCarReviewRepository.deleteById(id);
    }

    public Iterable<CarReview> findAll() {
        return inMemoryCarReviewRepository.findAll();
    }

    public List<CarReview> findCarReviewsByCarId(Long carId) {
        Validator.validateId(carId, "Car ID");
        CarReviewFilter filter = new CarReviewFilter().setTargetId(carId);
        return (List<CarReview>) inMemoryCarReviewRepository.findByFilter(filter);
    }
}