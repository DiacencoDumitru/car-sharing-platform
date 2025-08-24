package com.dynamiccarsharing.car.mapper;

import com.dynamiccarsharing.car.dto.CarCreateRequestDto;
import com.dynamiccarsharing.car.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.car.model.Car;
import com.dynamiccarsharing.contracts.dto.CarDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {LocationMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CarMapper {

    @Mapping(source = "location.id", target = "locationId")
    @Mapping(target = "instanceId", ignore = true)
    CarDto toDto(Car car);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "dto.locationId", target = "location")
    @Mapping(target = "status", constant = "AVAILABLE")
    @Mapping(target = "verificationStatus", constant = "PENDING")
    @Mapping(target = "reviews", ignore = true)
    @Mapping(source = "ownerId", target = "ownerId")
    Car toEntity(CarCreateRequestDto dto, Long ownerId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "verificationStatus", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(source = "dto.locationId", target = "location")
    void updateCarFromDto(CarUpdateRequestDto dto, @MappingTarget Car car);

    default Car fromId(Long id) {
        if (id == null) {
            return null;
        }
        return Car.builder().id(id).build();
    }
}