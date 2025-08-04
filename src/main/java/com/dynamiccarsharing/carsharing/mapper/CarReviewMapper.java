package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.dto.CarReviewCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.CarReviewDto;
import com.dynamiccarsharing.carsharing.dto.CarReviewUpdateRequestDto;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.CarReview;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CarReviewMapper {

    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "reviewer.id", target = "reviewerId")
    CarReviewDto toDto(CarReview review);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "carId", target = "car")
    @Mapping(source = "reviewerId", target = "reviewer.id")
    CarReview toEntity(CarReviewCreateRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(CarReviewUpdateRequestDto dto, @MappingTarget CarReview review);

    default Car map(Long carId) {
        if (carId == null) {
            return null;
        }
        return Car.builder().id(carId).build();
    }
}