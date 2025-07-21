package com.dynamiccarsharing.carsharing.service.jdbc;

import com.dynamiccarsharing.carsharing.exception.LocationNotFoundException;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.LocationFilter;
import com.dynamiccarsharing.carsharing.repository.jdbc.LocationRepositoryJdbcImpl;
import com.dynamiccarsharing.carsharing.service.interfaces.LocationService;
import com.dynamiccarsharing.carsharing.dto.LocationSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("locationService")
@Profile("jdbc")
@Transactional
public class LocationServiceJdbcImpl implements LocationService {

    private final LocationRepositoryJdbcImpl locationRepositoryJdbcImpl;

    public LocationServiceJdbcImpl(LocationRepositoryJdbcImpl locationRepositoryJdbcImpl) {
        this.locationRepositoryJdbcImpl = locationRepositoryJdbcImpl;
    }

    @Override
    public Location createLocation(Location location) {
        return locationRepositoryJdbcImpl.save(location);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Location> findById(Long id) {
        return locationRepositoryJdbcImpl.findById(id);
    }

    @Override
    public Location updateLocation(Long id, Location locationDetails) {
        locationRepositoryJdbcImpl.findById(id).orElseThrow(() -> new LocationNotFoundException("Location with ID " + id + " not found."));

        Location updatedLocation = Location.builder()
                .id(id)
                .city(locationDetails.getCity())
                .state(locationDetails.getState())
                .zipCode(locationDetails.getZipCode())
                .build();
        return locationRepositoryJdbcImpl.save(updatedLocation);
    }

    @Override
    public void deleteById(Long id) {
        locationRepositoryJdbcImpl.findById(id).orElseThrow(() -> new LocationNotFoundException("Location with ID " + id + " not found."));
        locationRepositoryJdbcImpl.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Location> searchLocations(LocationSearchCriteria criteria) {
        Filter<Location> filter = createFilterFromCriteria(criteria);
        try {
            return locationRepositoryJdbcImpl.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search for locations failed", e);
        }
    }

    private Filter<Location> createFilterFromCriteria(LocationSearchCriteria criteria) {
        return LocationFilter.of(
                criteria.getCity(),
                criteria.getState(),
                criteria.getZipCode()
        );
    }
}