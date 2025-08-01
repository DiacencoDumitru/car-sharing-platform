package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.criteria.LocationSearchCriteria;
import com.dynamiccarsharing.carsharing.exception.LocationNotFoundException;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.LocationFilter;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.LocationRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.LocationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("locationService")
@Transactional
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    public LocationServiceImpl(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public Location createLocation(Location location) {
        return locationRepository.save(location);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Location> findById(Long id) {
        return locationRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<Location> findAll() {
        return locationRepository.findAll();
    }

    @Override
    public Location updateLocation(Long id, Location locationDetails) {
        Location existingLocation = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location with ID " + id + " not found."));

        Location updatedLocation = existingLocation.toBuilder()
                .city(locationDetails.getCity())
                .state(locationDetails.getState())
                .zipCode(locationDetails.getZipCode())
                .build();
        return locationRepository.save(updatedLocation);
    }

    @Override
    public void deleteById(Long id) {
        if (locationRepository.findById(id).isEmpty()) {
            throw new LocationNotFoundException("Location with ID " + id + " not found.");
        }
        locationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Location> searchLocations(LocationSearchCriteria criteria) {
        Filter<Location> filter = LocationFilter.of(
                criteria.getCity(),
                criteria.getState(),
                criteria.getZipCode()
        );
        try {
            return locationRepository.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search for locations failed", e);
        }
    }
}