package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.repository.filter.BookingFilter;

import java.util.List;

public interface BookingRepository extends Repository<Booking, Long> {
    List<Booking> findByFilter(BookingFilter filter);
}
