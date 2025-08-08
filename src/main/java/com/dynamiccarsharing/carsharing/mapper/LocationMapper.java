package com.dynamiccarsharing.carsharing.mapper;

import com.dynamiccarsharing.carsharing.dto.LocationCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.LocationDto;
import com.dynamiccarsharing.carsharing.dto.LocationUpdateRequestDto;
import com.dynamiccarsharing.carsharing.model.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LocationMapper {

    LocationDto toDto(Location entity);

    @Mapping(target = "id", ignore = true)
    Location toEntity(LocationCreateRequestDto dto);

    void updateFromDto(LocationUpdateRequestDto dto, @MappingTarget Location entity);

    default Location fromId(Long id) {
        if (id == null) {
            return null;
        }
        return Location.builder().id(id).build();
    }
}