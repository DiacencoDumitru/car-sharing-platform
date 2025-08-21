package com.dynamiccarsharing.car.service;

import com.dynamiccarsharing.car.criteria.CarReviewSearchCriteria;
import com.dynamiccarsharing.car.dto.CarReviewCreateRequestDto;
import com.dynamiccarsharing.car.dto.CarReviewDto;
import com.dynamiccarsharing.car.dto.CarReviewUpdateRequestDto;
import com.dynamiccarsharing.car.exception.CarReviewNotFoundException;
import com.dynamiccarsharing.car.filter.CarReviewFilter;
import com.dynamiccarsharing.car.mapper.CarReviewMapper;
import com.dynamiccarsharing.car.model.CarReview;
import com.dynamiccarsharing.car.repository.CarRepository;
import com.dynamiccarsharing.car.repository.CarReviewRepository;
import com.dynamiccarsharing.car.service.interfaces.CarReviewService;
import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.exception.ValidationException;
import com.dynamiccarsharing.util.filter.Filter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("carReviewService")
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CarReviewServiceImpl implements CarReviewService {

    private final CarReviewRepository carReviewRepository;
    private final CarRepository carRepository;
    private final CarReviewMapper carReviewMapper;
    private final WebClient.Builder webClientBuilder;

    private WebClient userWebClient;

    @PostConstruct
    public void init() {
        this.userWebClient = webClientBuilder.baseUrl("lb://user-service").build();
    }

    @Override
    public CarReviewDto createReview(Long carId, CarReviewCreateRequestDto createDto) {
        validateCarExists(carId);
        validateUserExists(createDto.getReviewerId());

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
            throw new ServiceException("Search for car reviews failed", e);
        }
    }

    @Override
    public CarReviewDto updateReview(Long reviewId, CarReviewUpdateRequestDto updateDto) {
        CarReview reviewToUpdate = carReviewRepository.findById(reviewId).orElseThrow(() -> new CarReviewNotFoundException("CarReview with ID " + reviewId + " not found."));

        carReviewMapper.updateFromDto(updateDto, reviewToUpdate);

        CarReview updatedReview = carReviewRepository.save(reviewToUpdate);
        return carReviewMapper.toDto(updatedReview);
    }

    private void validateUserExists(Long userId) {
        try {
            UserDto user = userWebClient.get()
                    .uri("/" + userId)
                    .retrieve()
                    .bodyToMono(UserDto.class)
                    .block();

            if (user != null) {
                log.info("User validation handled by instance: {}", user.getInstanceId());
            }
        } catch (Exception e) {
            throw new ValidationException("Reviewer with User ID " + userId + " does not exist.");
        }
    }

    private void validateCarExists(Long carId) {
        if (carRepository.findById(carId).isEmpty()) {
            throw new ValidationException("Car with ID " + carId + " does not exist.");
        }
    }
}