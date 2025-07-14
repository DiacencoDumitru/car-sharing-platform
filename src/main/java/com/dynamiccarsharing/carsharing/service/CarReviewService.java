package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.exception.CarReviewNotFoundException;
import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.repository.CarReviewRepository;
import com.dynamiccarsharing.carsharing.repository.specification.CarReviewSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CarReviewService {

    private final CarReviewRepository carReviewRepository;

    public CarReviewService(CarReviewRepository carReviewRepository) {
        this.carReviewRepository = carReviewRepository;
    }

    public CarReview save(CarReview carReview) {
        return carReviewRepository.save(carReview);
    }

    public Optional<CarReview> findById(UUID id) {
        return carReviewRepository.findById(id);
    }

    public void deleteById(UUID id) {
        if (!carReviewRepository.existsById(id)) {
            throw new CarReviewNotFoundException("CarReview with ID " + id + " not found.");
        }
        carReviewRepository.deleteById(id);
    }

    public List<CarReview> findAll() {
        return carReviewRepository.findAll();
    }

    public List<CarReview> searchReviews(UUID carId, UUID reviewerId) {

        Specification<CarReview> spec = Specification
                .where(carId != null ? CarReviewSpecification.hasCarId(carId) : null)
                .and(reviewerId != null ? CarReviewSpecification.hasReviewerId(reviewerId) : null);

        return carReviewRepository.findAll(spec);
    }
}