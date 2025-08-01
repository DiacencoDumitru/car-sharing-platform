package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.dto.criteria.BookingSearchCriteria;

import java.util.List;
import java.util.Optional;

public interface BookingService {
    Booking save(Booking booking);

    Optional<Booking> findById(Long id);

    List<Booking> findAll();

    void deleteById(Long id);

    Booking approveBooking(Long bookingId);

    Booking completeBooking(Long bookingId);

    Booking cancelBooking(Long bookingId);

    Booking raiseDispute(Long bookingId, String disputeDescription);

    Booking resolveDispute(Long bookingId);

    List<Booking> findBookingsByRenterId(Long renterId);

    List<Booking> searchBookings(BookingSearchCriteria criteria);
}