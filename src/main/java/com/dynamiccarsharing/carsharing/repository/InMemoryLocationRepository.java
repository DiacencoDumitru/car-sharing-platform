package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryLocationRepository implements LocationRepository {
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
    public Iterable<Location> findAll() {
        return locationMap.values();
    }

    @Override
    public Iterable<Location> findByFilter(Filter<Location> filter) {
        return locationMap.values().stream().filter(filter::test).collect(Collectors.toList());
    }
}
