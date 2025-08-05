package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.BookingDto;
import com.dynamiccarsharing.carsharing.dto.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.carsharing.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingService {
    BookingDto save(BookingCreateRequestDto booking);

    Optional<BookingDto> findById(Long id);

    List<BookingDto> findAll();

    void deleteById(Long id);

    BookingDto approveBooking(Long bookingId);

    BookingDto completeBooking(Long bookingId);

    BookingDto cancelBooking(Long bookingId);

    BookingDto raiseDispute(Long bookingId, String disputeDescription);

    BookingDto resolveDispute(Long bookingId);

    List<Booking> findBookingsByRenterId(Long renterId);

    List<Booking> searchBookings(BookingSearchCriteria criteria);
}