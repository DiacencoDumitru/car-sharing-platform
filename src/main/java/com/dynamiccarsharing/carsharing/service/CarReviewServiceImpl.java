package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.criteria.CarReviewSearchCriteria;
import com.dynamiccarsharing.carsharing.exception.CarReviewNotFoundException;
import com.dynamiccarsharing.carsharing.filter.CarReviewFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.repository.CarReviewRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.CarReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("carReviewService")
@Transactional
public class CarReviewServiceImpl implements CarReviewService {

    private final CarReviewRepository carReviewRepository;

    public CarReviewServiceImpl(CarReviewRepository carReviewRepository) {
        this.carReviewRepository = carReviewRepository;
    }

    @Override
    public CarReview save(CarReview carReview) {
        return carReviewRepository.save(carReview);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CarReview> findById(Long id) {
        return carReviewRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        if (carReviewRepository.findById(id).isEmpty()) {
            throw new CarReviewNotFoundException("CarReview with ID " + id + " not found.");
        }
        carReviewRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarReview> findAll() {
        return (List<CarReview>) carReviewRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarReview> searchReviews(CarReviewSearchCriteria criteria) {
        Filter<CarReview> filter = CarReviewFilter.of(
                null,
                criteria.getReviewerId(),
                criteria.getCarId()
        );
        try {
            return carReviewRepository.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search for car reviews failed", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarReview> findByCarId(Long carId) {
        return carReviewRepository.findByCarId(carId);
    }

    @Override
    public CarReview updateReviewComment(Long reviewId, String newComment) {
        CarReview review = carReviewRepository.findById(reviewId).orElseThrow(() -> new CarReviewNotFoundException("CarReview with ID " + reviewId + " not found."));

        CarReview updatedReview = review.withComment(newComment);
        return carReviewRepository.save(updatedReview);
    }
}