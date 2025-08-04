package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.dto.LocationCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.LocationDto;
import com.dynamiccarsharing.carsharing.dto.LocationUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.criteria.LocationSearchCriteria;
import com.dynamiccarsharing.carsharing.model.Location;
<<<<<<< HEAD
=======
import com.dynamiccarsharing.carsharing.dto.criteria.LocationSearchCriteria;
>>>>>>> fix/controller-mvc-tests

import java.util.List;
import java.util.Optional;

public interface LocationService {
    LocationDto createLocation(LocationCreateRequestDto createDto);

    Optional<LocationDto> findLocationById(Long id);

<<<<<<< HEAD
    List<LocationDto> findAllLocations();

    LocationDto updateLocation(Long id, LocationUpdateRequestDto updateDto);
=======
    Iterable<Location> findAll();

    Location updateLocation(Long id, Location locationDetails);
>>>>>>> fix/controller-mvc-tests

    void deleteById(Long id);

    List<Location> searchLocations(LocationSearchCriteria criteria);
}