package com.dynamiccarsharing.car.mapper;

import com.dynamiccarsharing.car.dto.LocationCreateRequestDto;
import com.dynamiccarsharing.car.dto.LocationDto;
import com.dynamiccarsharing.car.dto.LocationUpdateRequestDto;
import com.dynamiccarsharing.car.model.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LocationMapper {

    LocationDto toDto(Location entity);

    @Mapping(target = "id", ignore = true)
    Location toEntity(LocationCreateRequestDto dto);

    @Mapping(target = "id", ignore = true)
    void updateFromDto(LocationUpdateRequestDto dto, @MappingTarget Location entity);

    default Location fromId(Long id) {
        if (id == null) {
            return null;
        }
        return Location.builder().id(id).build();
    }
}