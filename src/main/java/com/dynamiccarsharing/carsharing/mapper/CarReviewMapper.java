package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.dto.CarReviewCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.CarReviewDto;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.CarReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CarReviewMapper {

    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "reviewer.id", target = "reviewerId")
    CarReviewDto toDto(CarReview review);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "carId", target = "car")
    @Mapping(source = "dto.reviewerId", target = "reviewer.id")
    CarReview toEntity(CarReviewCreateRequestDto dto, Long carId);

    default Car map(Long carId) {
        if (carId == null) {
            return null;
        }
        return Car.builder().id(carId).build();
    }
}