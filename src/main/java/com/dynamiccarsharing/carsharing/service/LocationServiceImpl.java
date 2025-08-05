package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.LocationCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.LocationDto;
import com.dynamiccarsharing.carsharing.dto.LocationUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.criteria.LocationSearchCriteria;
import com.dynamiccarsharing.carsharing.exception.LocationNotFoundException;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.LocationFilter;
import com.dynamiccarsharing.carsharing.mapper.LocationMapper;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.LocationRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service("locationService")
@Transactional
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Override
    public LocationDto createLocation(LocationCreateRequestDto createDto) {
        Location location = locationMapper.toEntity(createDto);
        Location savedLocation = locationRepository.save(location);
        return locationMapper.toDto(savedLocation);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LocationDto> findLocationById(Long id) {
        return locationRepository.findById(id).map(locationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationDto> findAllLocations() {
        return StreamSupport.stream(locationRepository.findAll().spliterator(), false)
                .map(locationMapper::toDto)
                .toList();
    }


    @Override
    public LocationDto updateLocation(Long id, LocationUpdateRequestDto updateDto) {
        Location locationToUpdate = locationRepository.findById(id).orElseThrow(() -> new LocationNotFoundException("Location with ID " + id + " not found."));

        locationMapper.updateFromDto(updateDto, locationToUpdate);

        Location updatedLocation = locationRepository.save(locationToUpdate);

        return locationMapper.toDto(updatedLocation);
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