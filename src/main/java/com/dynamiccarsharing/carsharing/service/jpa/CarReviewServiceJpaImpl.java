package com.dynamiccarsharing.carsharing.service.jpa;

import com.dynamiccarsharing.carsharing.exception.CarReviewNotFoundException;
import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.repository.jpa.CarReviewJpaRepository;
import com.dynamiccarsharing.carsharing.specification.CarReviewSpecification;
import com.dynamiccarsharing.carsharing.service.interfaces.CarReviewService;
import com.dynamiccarsharing.carsharing.dto.CarReviewSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("carReviewService")
@Profile("jpa")
public class CarReviewServiceJpaImpl implements CarReviewService {

    private final CarReviewJpaRepository carReviewRepository;

    public CarReviewServiceJpaImpl(CarReviewJpaRepository carReviewRepository) {
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
        if (!carReviewRepository.existsById(id)) {
            throw new CarReviewNotFoundException("CarReview with ID " + id + " not found.");
        }
        carReviewRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarReview> findAll() {
        return carReviewRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarReview> searchReviews(CarReviewSearchCriteria criteria) {
        return carReviewRepository.findAll(
                CarReviewSpecification.withCriteria(
                        criteria.getReviewerId(),
                        criteria.getCarId()
                )
        );
    }
}