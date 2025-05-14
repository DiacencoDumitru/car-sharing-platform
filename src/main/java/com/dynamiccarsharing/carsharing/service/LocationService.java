package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.LocationRepository;
import com.dynamiccarsharing.carsharing.repository.filter.LocationFilter;
import com.dynamiccarsharing.carsharing.util.Validator;

import java.util.List;
import java.util.Optional;

public class LocationService {
    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Location save(Location location) {
        Validator.validateNonNull(location, "Location");
        return locationRepository.save(location);
    }

    public Optional<Location> findById(Long id) {
        Validator.validateId(id, "Location ID");
        return locationRepository.findById(id);
    }

    public void deleteById(Long id) {
        Validator.validateId(id, "Location ID");
        locationRepository.deleteById(id);
    }

    public Iterable<Location> findAll() {
        return locationRepository.findAll();
    }

    public List<Location> findLocationsByCity(String city) {
        Validator.validateNonEmptyString(city, "City");
        LocationFilter filter = new LocationFilter().setCity(city);
        return locationRepository.findByFilter(filter);
    }

    public List<Location> findLocationsByState(String state) {
        Validator.validateNonEmptyString(state, "State");
        LocationFilter filter = new LocationFilter().setState(state);
        return locationRepository.findByFilter(filter);
    }

    public List<Location> findLocationsByZipCode(String zipCode) {
        Validator.validateNonEmptyString(zipCode, "City");
        LocationFilter filter = new LocationFilter().setZipCode(zipCode);
        return locationRepository.findByFilter(filter);
    }
}
