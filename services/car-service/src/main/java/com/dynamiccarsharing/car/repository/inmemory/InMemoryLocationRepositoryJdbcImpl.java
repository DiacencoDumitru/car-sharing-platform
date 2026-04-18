package com.dynamiccarsharing.car.repository.inmemory;

import com.dynamiccarsharing.car.model.Location;
import com.dynamiccarsharing.car.repository.LocationRepository;
import com.dynamiccarsharing.util.filter.Filter;

import java.util.*;

public class InMemoryLocationRepositoryJdbcImpl implements LocationRepository {
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
        return locationMap.values().stream().filter(filter::test).toList();
    }

    @Override
    public List<Location> findAll() {
        return new ArrayList<>(locationMap.values());
    }

    @Override
    public List<Location> findByCityIgnoreCase(String city) {
        return locationMap.values().stream()
                .filter(location -> location.getCity().equalsIgnoreCase(city))
                .toList();
    }

    @Override
    public List<Location> findByStateIgnoreCase(String state) {
        return locationMap.values().stream()
                .filter(location -> location.getState().equalsIgnoreCase(state))
                .toList();
    }

    @Override
    public List<Location> findByZipCode(String zipCode) {
        return locationMap.values().stream()
                .filter(location -> location.getZipCode().equals(zipCode))
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return locationMap.containsKey(id);
    }
}