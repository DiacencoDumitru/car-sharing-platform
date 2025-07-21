package com.dynamiccarsharing.carsharing.service.jpa;

import com.dynamiccarsharing.carsharing.exception.LocationNotFoundException;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.jpa.LocationJpaRepository;
import com.dynamiccarsharing.carsharing.specification.LocationSpecification;
import com.dynamiccarsharing.carsharing.service.interfaces.LocationService;
import com.dynamiccarsharing.carsharing.dto.LocationSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("locationService")
@Profile("jpa")
@Transactional
public class LocationServiceJpaImpl implements LocationService {

    private final LocationJpaRepository locationRepository;

    public LocationServiceJpaImpl(LocationJpaRepository locationRepository) {
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
    public Location updateLocation(Long id, Location locationDetails) {
        Location existingLocation = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location with ID " + id + " not found."));

        Location updatedLocation = Location.builder()
                .id(existingLocation.getId())
                .city(locationDetails.getCity())
                .state(locationDetails.getState())
                .zipCode(locationDetails.getZipCode())
                .build();
        return locationRepository.save(updatedLocation);
    }

    @Override
    public void deleteById(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new LocationNotFoundException("Location with ID " + id + " not found.");
        }
        locationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Location> searchLocations(LocationSearchCriteria criteria) {
        return locationRepository.findAll(
                LocationSpecification.withCriteria(
                        criteria.getZipCode(),
                        criteria.getState(),
                        criteria.getCity()
                )
        );
    }
}