package com.dynamiccarsharing.booking.repository.jpa;

import com.dynamiccarsharing.booking.model.Booking;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Profile("jpa")
interface InternalBookingJpaRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    List<Booking> findByRenterId(Long renterId);

}