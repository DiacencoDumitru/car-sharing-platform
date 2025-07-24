package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Booking;

import java.util.List;

public interface BookingRepository extends Repository<Booking, Long> {

    List<Booking> findByRenterId(Long renterId);

    @Override
    List<Booking> findAll();
}
