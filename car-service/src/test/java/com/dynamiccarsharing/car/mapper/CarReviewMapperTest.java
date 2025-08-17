package com.dynamiccarsharing.car.mapper;

import com.dynamiccarsharing.car.dto.CarReviewCreateRequestDto;
import com.dynamiccarsharing.car.dto.CarReviewDto;
import com.dynamiccarsharing.car.dto.CarReviewUpdateRequestDto;
import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.car.model.CarReview;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CarReviewMapperImpl.class, CarMapperImpl.class, LocationMapperImpl.class})
class CarReviewMapperTest {

    @Autowired
    private CarReviewMapper carReviewMapper;

    @Test
    @DisplayName("Should correctly map CarReview entity to CarReviewDto")
    void toDto_ShouldMapCarReviewToDto() {
        CarReview review = CarReview.builder()
                .id(1L)
                .car(Car.builder().id(5L).build())
                .reviewerId(10L)
                .comment("Great car!")
                .build();

        CarReviewDto dto = carReviewMapper.toDto(review);

        assertNotNull(dto);
        assertEquals(review.getId(), dto.getId());
        assertEquals(review.getCar().getId(), dto.getCarId());
        assertEquals(review.getReviewerId(), dto.getReviewerId());
        assertEquals(review.getComment(), dto.getComment());
    }

    @Test
    @DisplayName("Should correctly map CarReviewCreateRequestDto to CarReview entity")
    void toEntity_ShouldMapCreateDtoToCarReview() {
        CarReviewCreateRequestDto dto = new CarReviewCreateRequestDto();
        dto.setCarId(5L);
        dto.setReviewerId(10L);
        dto.setComment("Very clean and smooth ride.");

        CarReview review = carReviewMapper.toEntity(dto);

        assertNotNull(review);
        assertNull(review.getId());
        assertNotNull(review.getCar());
        assertEquals(dto.getCarId(), review.getCar().getId());
        assertEquals(dto.getReviewerId(), review.getReviewerId());
        assertEquals(dto.getComment(), review.getComment());
    }

    @Test
    @DisplayName("Should update an existing CarReview from a CarReviewUpdateRequestDto")
    void updateFromDto_ShouldUpdateComment() {
        CarReview reviewToUpdate = CarReview.builder()
                .id(1L)
                .car(Car.builder().id(5L).build())
                .reviewerId(10L)
                .comment("Original comment.")
                .build();

        CarReviewUpdateRequestDto dto = new CarReviewUpdateRequestDto();
        dto.setComment("This is the updated comment.");

        carReviewMapper.updateFromDto(dto, reviewToUpdate);

        assertEquals(1L, reviewToUpdate.getId());
        assertEquals(5L, reviewToUpdate.getCar().getId());
        assertEquals(10L, reviewToUpdate.getReviewerId());
        assertEquals("This is the updated comment.", reviewToUpdate.getComment());
    }

    @Test
    @DisplayName("fromId should return CarReview with ID when ID is not null")
    void fromId_WithNonNullId_ShouldReturnCarReviewWithIdSet() {
        Long reviewId = 20L;

        CarReview result = carReviewMapper.fromId(reviewId);

        assertNotNull(result);
        assertEquals(reviewId, result.getId());
    }

    @Test
    @DisplayName("fromId should return null when ID is null")
    void fromId_WithNullId_ShouldReturnNull() {
        CarReview result = carReviewMapper.fromId(null);

        assertNull(result);
    }
}