package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.dto.CarCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.CarDto;
import com.dynamiccarsharing.carsharing.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.carsharing.model.Car;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {LocationMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CarMapper {

    @Mapping(source = "location.id", target = "locationId")
    CarDto toDto(Car car);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "locationId", target = "location")
    @Mapping(target = "status", constant = "AVAILABLE")
    @Mapping(target = "verificationStatus", constant = "PENDING")
    @Mapping(target = "owners", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    Car toEntity(CarCreateRequestDto dto);

    @Mapping(source = "locationId", target = "location")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Car updateCarFromDto(CarUpdateRequestDto dto, @MappingTarget Car car);

    default Car fromId(Long id) {
        if (id == null) {
            return null;
        }
        return Car.builder().id(id).build();
    }
}