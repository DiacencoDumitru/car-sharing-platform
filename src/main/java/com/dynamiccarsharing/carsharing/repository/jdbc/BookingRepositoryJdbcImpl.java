package com.dynamiccarsharing.carsharing.repository.jdbc;

import com.dynamiccarsharing.carsharing.model.Booking;

import java.util.List;

public interface BookingRepositoryJdbcImpl extends Repository<Booking, Long> {
    List<Booking> findByRenterId(Long renterId);
}

