package com.dynamiccarsharing.car.service.interfaces;

import com.dynamiccarsharing.car.criteria.LocationSearchCriteria;
import com.dynamiccarsharing.contracts.dto.LocationCreateRequestDto;
import com.dynamiccarsharing.contracts.dto.LocationDto;
import com.dynamiccarsharing.contracts.dto.LocationUpdateRequestDto;
import com.dynamiccarsharing.car.model.Location;

import java.util.List;
import java.util.Optional;

public interface LocationService {
    LocationDto createLocation(LocationCreateRequestDto createDto);

    Optional<LocationDto> findLocationById(Long id);

    List<LocationDto> findAllLocations();

    LocationDto updateLocation(Long id, LocationUpdateRequestDto updateDto);

    void deleteById(Long id);

    List<Location> searchLocations(LocationSearchCriteria criteria);
}