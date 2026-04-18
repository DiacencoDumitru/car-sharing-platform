package com.dynamiccarsharing.car.service;

import com.dynamiccarsharing.car.criteria.LocationSearchCriteria;
import com.dynamiccarsharing.car.dto.LocationCreateRequestDto;
import com.dynamiccarsharing.car.dto.LocationDto;
import com.dynamiccarsharing.car.dto.LocationUpdateRequestDto;
import com.dynamiccarsharing.car.exception.LocationNotFoundException;
import com.dynamiccarsharing.car.filter.LocationFilter;
import com.dynamiccarsharing.car.mapper.LocationMapper;
import com.dynamiccarsharing.car.model.Location;
import com.dynamiccarsharing.car.repository.LocationRepository;
import com.dynamiccarsharing.car.service.interfaces.LocationService;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.filter.Filter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("locationService")
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Override
    @Caching(
            put = { @CachePut(cacheNames = "locationById", key = "#result.id") },
            evict = { @CacheEvict(cacheNames = "allLocations", allEntries = true) }
    )
    public LocationDto createLocation(LocationCreateRequestDto createDto) {
        Location location = locationMapper.toEntity(createDto);
        Location savedLocation = locationRepository.save(location);
        log.info("Created new location with id {}. Evicting 'allLocations' cache.", savedLocation.getId());
        return locationMapper.toDto(savedLocation);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "locationById", key = "#id")
    public Optional<LocationDto> findLocationById(Long id) {
        log.debug("Cache MISS for locationById -> loading from DB for id={}", id);
        return locationRepository.findById(id).map(locationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable("allLocations")
    public List<LocationDto> findAllLocations() {
        log.debug("Cache MISS for allLocations -> loading from DB.");
        return locationRepository.findAll().stream()
                .map(locationMapper::toDto)
                .toList();
    }


    @Override
    @Caching(
            put = { @CachePut(cacheNames = "locationById", key = "#id") },
            evict = { @CacheEvict(cacheNames = "allLocations", allEntries = true) }
    )
    public LocationDto updateLocation(Long id, LocationUpdateRequestDto updateDto) {
        Location locationToUpdate = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location with ID " + id + " not found."));

        locationMapper.updateFromDto(updateDto, locationToUpdate);
        Location updatedLocation = locationRepository.save(locationToUpdate);
        log.info("Updated location with id {}. Evicting 'allLocations' cache.", updatedLocation.getId());
        return locationMapper.toDto(updatedLocation);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "locationById", key = "#id"),
            @CacheEvict(cacheNames = "allLocations", allEntries = true)
    })
    public void deleteById(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new LocationNotFoundException("Location with ID " + id + " not found.");
        }
        locationRepository.deleteById(id);
        log.info("Deleted location with id {}. Evicting caches.", id);
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
            throw new ServiceException("Search for locations failed", e);
        }
    }
}