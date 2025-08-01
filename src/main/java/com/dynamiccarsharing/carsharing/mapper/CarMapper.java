package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.dto.CarCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.CarDto;
import com.dynamiccarsharing.carsharing.dto.CarUpdateRequestDto;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CarMapper {

    @Mapping(source = "location.id", target = "locationId")
    CarDto toDto(Car car);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "locationId", target = "location.id")
    @Mapping(target = "status", constant = "AVAILABLE")
    @Mapping(target = "verificationStatus", constant = "PENDING")
    @Mapping(target = "owners", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    Car toEntity(CarCreateRequestDto dto);

    @Mapping(source = "locationId", target = "location")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Car updateCarFromDto(CarUpdateRequestDto dto, @MappingTarget Car car);

    default Location toLocation(Long locationId) {
        if (locationId == null) {
            return null;
        }
        return Location.builder().id(locationId).build();
    }
}