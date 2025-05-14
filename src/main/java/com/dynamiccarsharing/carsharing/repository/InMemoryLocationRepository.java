package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.filter.LocationFilter;

import java.util.*;

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
    public List<Location> findByFilter(LocationFilter filter) {
        return locationMap.values().stream().filter(filter::test).toList();
    }
}
