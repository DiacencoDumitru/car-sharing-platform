package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.exception.LocationNotFoundException;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.LocationRepository;
import com.dynamiccarsharing.carsharing.repository.specification.LocationSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class LocationService {

    private final LocationRepository locationRepository;

    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public Location save(Location location) {
        return locationRepository.save(location);
    }

    public Optional<Location> findById(UUID id) {
        return locationRepository.findById(id);
    }

    public void deleteById(UUID id) {
        if (!locationRepository.existsById(id)) {
            throw new LocationNotFoundException("Location with ID " + id + " not found.");
        }
        locationRepository.deleteById(id);
    }

    public List<Location> findAll() {
        return locationRepository.findAll();
    }

    public List<Location> findLocationsByCity(String city) {
        return locationRepository.findByCityIgnoreCase(city);
    }

    public List<Location> findLocationsByState(String state) {
        return locationRepository.findByStateIgnoreCase(state);
    }

    public List<Location> findLocationsByZipCode(String zipCode) {
        return locationRepository.findByZipCode(zipCode);
    }

    public List<Location> searchLocations(String city, String state) {
        Specification<Location> spec = Specification
                .where(city != null ? LocationSpecification.cityContains(city) : null)
                .and(state != null ? LocationSpecification.stateContains(state) : null);

        return locationRepository.findAll(spec);
    }
}