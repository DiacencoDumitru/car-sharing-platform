package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.dto.criteria.LocationSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface LocationService {
    Location createLocation(Location location);

    Optional<Location> findById(Long id);

    Iterable<Location> findAll();

    Location updateLocation(Long id, Location locationDetails);

    void deleteById(Long id);

    List<Location> searchLocations(LocationSearchCriteria criteria);
}