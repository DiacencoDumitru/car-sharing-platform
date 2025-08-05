package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.dto.LocationCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.LocationDto;
import com.dynamiccarsharing.carsharing.dto.LocationUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.criteria.LocationSearchCriteria;
import com.dynamiccarsharing.carsharing.model.Location;

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