package com.dynamiccarsharing.car.service;

import com.dynamiccarsharing.car.dto.CarReviewCreateRequestDto;
import com.dynamiccarsharing.car.dto.CarReviewDto;
import com.dynamiccarsharing.car.dto.CarReviewUpdateRequestDto;
import com.dynamiccarsharing.car.exception.CarReviewNotFoundException;
import com.dynamiccarsharing.car.mapper.CarReviewMapper;
import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.model.CarReview;
import com.dynamiccarsharing.car.repository.CarRepository;
import com.dynamiccarsharing.car.repository.CarReviewRepository;
import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.util.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarReviewServiceImplTest {

    @Mock
    private CarReviewRepository carReviewRepository;
    @Mock
    private CarRepository carRepository;
    @Mock
    private CarReviewMapper carReviewMapper;
    @Mock
    private WebClient.Builder webClientBuilder;
    @Mock
    private WebClient userWebClient;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache carReviewsCache;

    private CarReviewServiceImpl carReviewService;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(userWebClient);

        carReviewService = new CarReviewServiceImpl(
                carReviewRepository,
                carRepository,
                carReviewMapper,
                webClientBuilder,
                cacheManager
        );

        carReviewService.init();
    }

    @Test
    @DisplayName("Should create review when car and user exist")
    void createReview_whenValid_shouldMapAndSaveAndReturnDto() {
        Long carId = 1L;
        Long reviewerId = 100L;
        var createDto = new CarReviewCreateRequestDto();
        createDto.setComment("Test comment");
        createDto.setReviewerId(reviewerId);

        var reviewToSave = new CarReview();
        var savedReview = CarReview.builder().id(1L).build();
        var expectedDto = new CarReviewDto();
        expectedDto.setId(1L);

        when(carRepository.findById(carId)).thenReturn(Optional.of(new Car()));
        mockUserWebClient(reviewerId, new UserDto());

        when(carReviewMapper.toEntity(any(CarReviewCreateRequestDto.class))).thenReturn(reviewToSave);
        when(carReviewRepository.save(reviewToSave)).thenReturn(savedReview);
        when(carReviewMapper.toDto(savedReview)).thenReturn(expectedDto);

        CarReviewDto result = carReviewService.createReview(carId, createDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(carRepository).findById(carId);
        verify(carReviewRepository).save(reviewToSave);
    }

    @Test
    @DisplayName("Should fail to create review when user does not exist")
    void createReview_whenUserNotFound_shouldThrowException() {
        Long carId = 1L;
        Long reviewerId = 100L;
        CarReviewCreateRequestDto createDto = new CarReviewCreateRequestDto();
        createDto.setReviewerId(reviewerId);

        when(carRepository.findById(carId)).thenReturn(Optional.of(new Car()));
        mockUserWebClient(reviewerId, null);

        assertThrows(ValidationException.class, () -> carReviewService.createReview(carId, createDto));
    }

    @Test
    @DisplayName("Should fail to create review when car does not exist")
    void createReview_whenCarNotFound_shouldThrowException() {
        Long carId = 1L;
        CarReviewCreateRequestDto createDto = new CarReviewCreateRequestDto();
        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> carReviewService.createReview(carId, createDto));
    }

    @Test
    @DisplayName("deleteById() should evict cache and delete review when review exists")
    void deleteById_whenReviewExists_shouldEvictCacheAndDelete() {
        Long reviewId = 1L;
        Long carId = 10L;

        var car = Car.builder().id(carId).build();
        var review = CarReview.builder().id(reviewId).car(car).build();

        when(carReviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(cacheManager.getCache("carReviewsByCarId")).thenReturn(carReviewsCache);
        doNothing().when(carReviewRepository).deleteById(reviewId);

        carReviewService.deleteById(reviewId);

        verify(carReviewRepository).deleteById(reviewId);
        verify(carReviewsCache).evict(carId);
    }

    @Test
    @DisplayName("updateReview() should update review when it exists")
    void updateReview_whenExists_shouldUpdateAndReturnDto() {
        Long reviewId = 1L;
        Long carId = 10L;
        String newComment = "New Comment";
        var updateDto = new CarReviewUpdateRequestDto();
        updateDto.setComment(newComment);

        var car = Car.builder().id(carId).build();
        var existingReview = CarReview.builder().id(reviewId).comment("Old Comment").car(car).build();

        var expectedDto = new CarReviewDto();
        expectedDto.setComment(newComment);
        expectedDto.setCarId(carId);

        when(carReviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
        when(carReviewRepository.save(any(CarReview.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carReviewMapper.toDto(any(CarReview.class))).thenReturn(expectedDto);

        doAnswer(invocation -> {
            CarReview reviewArg = invocation.getArgument(1);
            reviewArg.setComment(newComment);
            return null;
        }).when(carReviewMapper).updateFromDto(any(CarReviewUpdateRequestDto.class), any(CarReview.class));

        CarReviewDto result = carReviewService.updateReview(reviewId, updateDto);

        assertEquals(newComment, result.getComment());
        verify(carReviewRepository).save(argThat(review -> review.getComment().equals(newComment)));
    }

    @Test
    @DisplayName("updateReview() should throw exception when review does not exist")
    void updateReview_whenNotExists_shouldThrowException() {
        Long reviewId = 1L;
        CarReviewUpdateRequestDto updateDto = new CarReviewUpdateRequestDto();
        when(carReviewRepository.findById(reviewId)).thenReturn(Optional.empty());
        assertThrows(CarReviewNotFoundException.class, () -> carReviewService.updateReview(reviewId, updateDto));
    }

    @Test
    @DisplayName("findById() should return empty optional when review does not exist")
    void findById_whenReviewDoesNotExist_shouldReturnEmptyOptional() {
        Long reviewId = 1L;
        when(carReviewRepository.findById(reviewId)).thenReturn(Optional.empty());
        Optional<CarReviewDto> result = carReviewService.findById(reviewId);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("findByCarId() should map and return a list of DTOs")
    void findByCarId_shouldMapAndReturnDtoList() {
        Long carId = 1L;
        CarReview reviewEntity = CarReview.builder().id(1L).build();
        when(carReviewRepository.findByCarId(carId)).thenReturn(Collections.singletonList(reviewEntity));
        when(carReviewMapper.toDto(reviewEntity)).thenReturn(new CarReviewDto());
        List<CarReviewDto> result = carReviewService.findByCarId(carId);
        assertEquals(1, result.size());
    }


    private void mockUserWebClient(Long userId, UserDto responseDto) {
        var requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        var requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        var responseSpec = mock(WebClient.ResponseSpec.class);
        var mono = responseDto != null ? Mono.just(responseDto) : Mono.error(new RuntimeException("Simulated 404 Not Found"));

        when(userWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/api/v1/users/{id}", userId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(UserDto.class)).thenReturn((Mono<UserDto>) mono);
    }
}