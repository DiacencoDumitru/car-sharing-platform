package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.InMemoryLocationRepository;
import com.dynamiccarsharing.carsharing.repository.filter.LocationFilter;
import com.dynamiccarsharing.carsharing.util.Validator;

import java.util.List;
import java.util.Optional;

public class LocationService {
    private final InMemoryLocationRepository inMemoryLocationRepository;

    public LocationService(InMemoryLocationRepository inMemoryLocationRepository) {
        this.inMemoryLocationRepository = inMemoryLocationRepository;
    }

    public Location save(Location location) {
        Validator.validateNonNull(location, "Location");
        return inMemoryLocationRepository.save(location);
    }

    public Optional<Location> findById(Long id) {
        Validator.validateId(id, "ID");
        return inMemoryLocationRepository.findById(id);
    }

    public void delete(Long id) {
        Validator.validateId(id, "ID");
        inMemoryLocationRepository.deleteById(id);
    }

    public Iterable<Location> findAll() {
        return inMemoryLocationRepository.findAll();
    }

    public List<Location> findLocationsByCity(String city) {
        Validator.validateNonEmptyString(city, "City");
        LocationFilter filter = new LocationFilter().setCity(city);
        return (List<Location>) inMemoryLocationRepository.findByFilter(filter);
    }
}
