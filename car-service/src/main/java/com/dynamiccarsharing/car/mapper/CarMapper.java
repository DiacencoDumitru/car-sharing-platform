package com.dynamiccarsharing.car.mapper;

import com.dynamiccarsharing.contracts.dto.CarCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.car.model.Car;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {LocationMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CarMapper {

    @Mapping(source = "location.id", target = "locationId")
    CarDto toDto(Car car);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "locationId", target = "location")
    @Mapping(target = "status", constant = "AVAILABLE")
    @Mapping(target = "verificationStatus", constant = "PENDING")
    @Mapping(target = "reviews", ignore = true)
    Car toEntity(CarCreateRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "verificationStatus", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(source = "locationId", target = "location")
    Car updateCarFromDto(CarUpdateRequestDto dto, @MappingTarget Car car);

    default Car fromId(Long id) {
        if (id == null) {
            return null;
        }
        return Car.builder().id(id).build();
    }
}