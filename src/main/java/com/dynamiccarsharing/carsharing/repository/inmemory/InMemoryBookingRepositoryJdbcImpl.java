package com.dynamiccarsharing.carsharing.repository.inmemory;

import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.repository.jdbc.BookingRepositoryJdbcImpl;
import com.dynamiccarsharing.carsharing.filter.Filter;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryBookingRepositoryJdbcImpl implements BookingRepositoryJdbcImpl {
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
    public List<Booking> findByFilter(Filter<Booking> filter) {
        return bookingMap.values().stream().filter(filter::test).collect(Collectors.toList());
    }

    @Override
    public Iterable<Booking> findAll() {
        return bookingMap.values();
    }

    @Override
    public List<Booking> findByRenterId(Long renterId) {
        return bookingMap.values().stream()
                .filter(booking -> booking.getRenter() != null && booking.getRenter().getId().equals(renterId))
                .collect(Collectors.toList());
    }
}