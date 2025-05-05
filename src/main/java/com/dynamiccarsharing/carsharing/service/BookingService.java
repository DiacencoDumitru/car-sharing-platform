package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.repository.BookingRepository;

import java.util.Map;

public class BookingService {

    private final BookingRepository bookingRepository = new BookingRepository();

    public void saveBooking(Booking booking) {
        bookingRepository.save(booking);
    }

    public Booking findBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public Booking findBookingByRenterId(Long renterId) {
        return bookingRepository.findByField(String.valueOf(renterId));
    }

    public void updateBooking(Booking booking) {
        bookingRepository.update(booking);
    }

    public void deleteBooking(Long id) {
        bookingRepository.delete(id);
    }

    public Map<Long, Booking> findAllBookings() {
        return bookingRepository.findAll();
    }

    public Map<Long, Booking> findBookingsByFilter(String field, String value) {
        return bookingRepository.findByFilter(field, value);
    }
}