package com.dynamiccarsharing.carsharing.repository.inmemory;

import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.jdbc.LocationRepositoryJdbcImpl;
import com.dynamiccarsharing.carsharing.filter.Filter;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryLocationRepositoryJdbcImpl implements LocationRepositoryJdbcImpl {
    private final Map<Long, Location> locationMap = new HashMap<>();

    @Override
    public Location save(Location location) {
        locationMap.put(location.getId(), location);
        return location;
    }

    @Override
    public Optional<Location> findById(Long id) {
        return Optional.ofNullable(locationMap.get(id));
    }

    @Override
    public void deleteById(Long id) {
        locationMap.remove(id);
    }

    @Override
    public List<Location> findByFilter(Filter<Location> filter) {
        return locationMap.values().stream().filter(filter::test).collect(Collectors.toList());
    }

    @Override
    public Iterable<Location> findAll() {
        return locationMap.values();
    }

    @Override
    public List<Location> findByStateIgnoreCase(String state) {
        return locationMap.values().stream()
                .filter(location -> location.getState().equalsIgnoreCase(state))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Location> findByZipCode(String zipCode) {
        return locationMap.values().stream()
                .filter(location -> location.getZipCode().equals(zipCode))
                .findFirst();
    }
}