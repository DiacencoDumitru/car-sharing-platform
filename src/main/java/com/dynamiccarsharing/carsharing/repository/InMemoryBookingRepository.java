package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.repository.filter.BookingFilter;

import java.util.*;

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

    public List<Booking> findByFilter(BookingFilter filter) {
        return bookingMap.values().stream().filter(filter::test).toList();
    }
}
