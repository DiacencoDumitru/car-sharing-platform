package com.dynamiccarsharing.carsharing.service;

<<<<<<< HEAD
import com.dynamiccarsharing.carsharing.dto.CarReviewCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.CarReviewDto;
import com.dynamiccarsharing.carsharing.dto.CarReviewUpdateRequestDto;
=======
>>>>>>> fix/controller-mvc-tests
import com.dynamiccarsharing.carsharing.dto.criteria.CarReviewSearchCriteria;
import com.dynamiccarsharing.carsharing.exception.CarReviewNotFoundException;
import com.dynamiccarsharing.carsharing.filter.CarReviewFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.mapper.CarReviewMapper;
import com.dynamiccarsharing.carsharing.model.CarReview;
import com.dynamiccarsharing.carsharing.repository.CarReviewRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.CarReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("carReviewService")
@Transactional
@RequiredArgsConstructor
public class CarReviewServiceImpl implements CarReviewService {

    private final CarReviewRepository carReviewRepository;
    private final CarReviewMapper carReviewMapper;

    @Override
    public CarReviewDto createReview(Long carId, CarReviewCreateRequestDto createDto) {
        createDto.setCarId(carId);
        CarReview review = carReviewMapper.toEntity(createDto);
        CarReview savedReview = carReviewRepository.save(review);
        return carReviewMapper.toDto(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CarReviewDto> findById(Long id) {
        return carReviewRepository.findById(id).map(carReviewMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarReviewDto> findByCarId(Long carId) {
        return carReviewRepository.findByCarId(carId).stream().map(carReviewMapper::toDto).toList();
    }

    @Override
    public void deleteById(Long id) {
        if (carReviewRepository.findById(id).isPresent()) {
            carReviewRepository.deleteById(id);
        } else {
            throw new CarReviewNotFoundException("Car review with ID " + id + " not found.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarReviewDto> findAll() {
        return carReviewRepository.findAll().stream().map(carReviewMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarReview> searchReviews(CarReviewSearchCriteria criteria) {
        Filter<CarReview> filter = CarReviewFilter.of(
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
<<<<<<< HEAD
    public CarReviewDto updateReview(Long reviewId, CarReviewUpdateRequestDto updateDto) {
        CarReview reviewToUpdate = carReviewRepository.findById(reviewId).orElseThrow(() -> new CarReviewNotFoundException("CarReview with ID " + reviewId + " not found."));

        carReviewMapper.updateFromDto(updateDto, reviewToUpdate);

        CarReview updatedReview = carReviewRepository.save(reviewToUpdate);
        return carReviewMapper.toDto(updatedReview);
=======
    @Transactional(readOnly = true)
    public List<CarReview> findByCarId(Long carId) {
        return carReviewRepository.findByCarId(carId);
    }

    @Override
    public CarReview updateReviewComment(Long reviewId, String newComment) {
        CarReview review = carReviewRepository.findById(reviewId).orElseThrow(() -> new CarReviewNotFoundException("CarReview with ID " + reviewId + " not found."));

        CarReview updatedReview = review.withComment(newComment);
        return carReviewRepository.save(updatedReview);
>>>>>>> fix/controller-mvc-tests
    }
}