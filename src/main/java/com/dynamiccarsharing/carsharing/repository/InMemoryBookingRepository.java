package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryBookingRepository implements BookingRepository {
    private final Map<Long, Booking> bookingMap = new HashMap<>();

    @Override
    public Booking save(Booking booking) {
        bookingMap.put(booking.getId(), booking);
        return booking;
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return Optional.ofNullable(bookingMap.get(id));
    }

    @Override
    public void deleteById(Long id) {
        bookingMap.remove(id);
    }

    @Override
    public Iterable<Booking> findAll() {
        return bookingMap.values();
    }

    public Iterable<Booking> findByFilter(Filter<Booking> filter) {
        return bookingMap.values().stream().filter(filter::test).collect(Collectors.toList());
    }
}
