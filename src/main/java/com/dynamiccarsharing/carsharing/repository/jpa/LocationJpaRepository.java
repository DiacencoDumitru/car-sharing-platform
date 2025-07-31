package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.filter.LocationFilter;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.LocationRepository;
import com.dynamiccarsharing.carsharing.specification.LocationSpecification;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Profile("jpa")
@Repository
public interface LocationJpaRepository extends JpaRepository<Location, Long>, JpaSpecificationExecutor<Location>, LocationRepository {

    @Override
    List<Location> findByCityIgnoreCase(String city);

    @Override
    List<Location> findByStateIgnoreCase(String state);

    @Override
    List<Location> findByZipCode(String zipCode);

    @Override
    default List<Location> findByFilter(Filter<Location> filter) throws SQLException {
        if (!(filter instanceof LocationFilter locationFilter)) {
            throw new IllegalArgumentException("Filter must be an instance of LocationFilter for JPA search.");
        }
        return findAll(LocationSpecification.withCriteria(
                locationFilter.getZipCode(),
                locationFilter.getState(),
                locationFilter.getCity()
        ));
    }
}