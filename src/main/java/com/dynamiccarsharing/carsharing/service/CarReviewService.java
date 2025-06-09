package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dao.CarReviewDao;
import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.repository.filter.CarReviewFilter;
import com.dynamiccarsharing.carsharing.util.Validator;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CarReviewService {
    private final CarReviewDao carReviewRepository;

    public CarReviewService(CarReviewDao carReviewRepository) {
        this.carReviewRepository = carReviewRepository;
    }

    public CarReview save(CarReview carReview)  {
        Validator.validateNonNull(carReview, "Car Review");
        return carReviewRepository.save(carReview);
    }

    public Optional<CarReview> findById(Long id) {
        Validator.validateId(id, "CarReview ID");
        return carReviewRepository.findById(id);
    }

    public void deleteById(Long id) {
        Validator.validateId(id, "CarReview ID");
        carReviewRepository.deleteById(id);
    }

    public Iterable<CarReview> findAll() {
        return carReviewRepository.findAll();
    }

    public List<CarReview> findCarReviewsByCarId(Long carId) throws SQLException {
        Validator.validateId(carId, "Car ID");
        CarReviewFilter filter = CarReviewFilter.ofCarId(carId);
        return carReviewRepository.findByFilter(filter);
    }

    public List<CarReview> findCarReviewsByReviewerId(Long reviewerId) throws SQLException {
        Validator.validateId(reviewerId, "Reviewer ID");
        CarReviewFilter filter = CarReviewFilter.ofReviewerId(reviewerId);
        return carReviewRepository.findByFilter(filter);
    }
}