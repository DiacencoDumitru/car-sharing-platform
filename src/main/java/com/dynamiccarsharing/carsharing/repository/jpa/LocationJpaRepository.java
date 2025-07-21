package com.dynamiccarsharing.carsharing.repository.jpa;

import com.dynamiccarsharing.carsharing.model.Location;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Profile("jpa")
@Repository
public interface LocationJpaRepository extends JpaRepository<Location, Long>, JpaSpecificationExecutor<Location> {

    List<Location> findByCityIgnoreCase(String city);

    List<Location> findByStateIgnoreCase(String state);

    List<Location> findByZipCode(String zipCode);
}