package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.filter.LocationFilter;

import java.util.List;

public interface LocationRepository extends Repository<Location, Long> {
    List<Location> findByFilter(LocationFilter filter);
}
