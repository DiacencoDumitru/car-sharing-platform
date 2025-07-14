package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID>, JpaSpecificationExecutor<Location> {

    List<Location> findByCityIgnoreCase(String city);

    List<Location> findByStateIgnoreCase(String state);

    List<Location> findByZipCode(String zipCode);
}