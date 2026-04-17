package com.dynamiccarsharing.car.mapper;

import com.dynamiccarsharing.car.dto.CarReviewCreateRequestDto;
import com.dynamiccarsharing.car.dto.CarReviewDto;
import com.dynamiccarsharing.car.dto.CarReviewUpdateRequestDto;
import com.dynamiccarsharing.car.model.CarReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {CarMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CarReviewMapper {

    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "reviewerId", target = "reviewerId")
    @Mapping(source = "bookingId", target = "bookingId")
    @Mapping(source = "rating", target = "rating")
    CarReviewDto toDto(CarReview review);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "carId", target = "car")
    @Mapping(source = "reviewerId", target = "reviewerId")
    @Mapping(source = "bookingId", target = "bookingId")
    @Mapping(source = "rating", target = "rating")
    CarReview toEntity(CarReviewCreateRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "reviewerId", ignore = true)
    @Mapping(target = "bookingId", ignore = true)
    void updateFromDto(CarReviewUpdateRequestDto dto, @MappingTarget CarReview review);

    default CarReview fromId(Long carReviewId) {
        if (carReviewId == null) {
            return null;
        }
        return CarReview.builder().id(carReviewId).build();
    }
}