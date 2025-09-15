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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final CacheManager cacheManager;

    private WebClient userWebClient;

    @PostConstruct
    public void init() {
        this.userWebClient = webClientBuilder.baseUrl("lb://user-service").build();
    }

    @Override
    @CacheEvict(cacheNames = "carReviewsByCarId", key = "#carId")
    public CarReviewDto createReview(Long carId, CarReviewCreateRequestDto createDto) {
        log.info("Creating review for carId: {}", carId);
        validateCarExists(carId);
        validateUserExists(createDto.getReviewerId());

        createDto.setCarId(carId);
        CarReview review = carReviewMapper.toEntity(createDto);
        CarReview savedReview = carReviewRepository.save(review);
        log.info("Successfully created review {} for carId: {}", savedReview.getId(), carId);
        return carReviewMapper.toDto(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CarReviewDto> findById(Long id) {
        return carReviewRepository.findById(id).map(carReviewMapper::toDto);
    }

    @Override
    @Cacheable(cacheNames = "carReviewsByCarId", key = "#carId")
    public List<CarReviewDto> findByCarId(Long carId) {
        log.debug("Cache MISS for carReviewsByCarId -> loading from DB for carId={}", carId);
        return carReviewRepository.findByCarId(carId).stream().map(carReviewMapper::toDto).toList();
    }

    @Override
    public void deleteById(Long id) {
        CarReview review = carReviewRepository.findById(id)
                .orElseThrow(() -> new CarReviewNotFoundException("Car review with ID " + id + " not found."));

        Long carId = review.getCar().getId();

        carReviewRepository.deleteById(id);

        Cache carReviewsCache = cacheManager.getCache("carReviewsByCarId");
        if (carReviewsCache != null) {
            carReviewsCache.evict(carId);
            log.info("Programmatically evicted cache 'carReviewsByCarId' for carId: {}", carId);
        }

        log.info("Deleted review with id {} for carId {}", id, carId);
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
    @CacheEvict(cacheNames = "carReviewsByCarId", key = "#result.carId")
    public CarReviewDto updateReview(Long reviewId, CarReviewUpdateRequestDto updateDto) {
        CarReview reviewToUpdate = carReviewRepository.findById(reviewId)
                .orElseThrow(() -> new CarReviewNotFoundException("CarReview with ID " + reviewId + " not found."));

        carReviewMapper.updateFromDto(updateDto, reviewToUpdate);
        CarReview updatedReview = carReviewRepository.save(reviewToUpdate);
        log.info("Updated review {} for carId {}. Evicting cache via annotation.", reviewId, updatedReview.getCar().getId());

        return carReviewMapper.toDto(updatedReview);
    }

    private void validateUserExists(Long userId) {
        try {
            UserDto user = userWebClient.get()
                    .uri("/api/v1/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserDto.class)
                    .block();

            if (user == null) {
                throw new ValidationException("Reviewer with User ID " + userId + " does not exist.");
            }
            log.info("User {} validation successful. Handled by instance: {}", userId, user.getInstanceId());
        } catch (Exception e) {
            log.error("Error validating user with ID {}: {}", userId, e.getMessage());
            throw new ValidationException("Reviewer with User ID " + userId + " could not be validated or does not exist.");
        }
    }

    private void validateCarExists(Long carId) {
        if (carRepository.findById(carId).isEmpty()) {
            throw new ValidationException("Car with ID " + carId + " does not exist.");
        }
    }
}