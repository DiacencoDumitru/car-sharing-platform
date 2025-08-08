package com.dynamiccarsharing.carsharing.service.interfaces;

import com.dynamiccarsharing.carsharing.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.BookingDto;
import com.dynamiccarsharing.carsharing.dto.BookingStatusUpdateRequestDto;
import com.dynamiccarsharing.carsharing.dto.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.carsharing.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BookingService {
    BookingDto save(BookingCreateRequestDto booking);

    Optional<BookingDto> findById(Long id);

    Page<BookingDto> findAll(BookingSearchCriteria criteria, Pageable pageable);

    void deleteById(Long id);

    BookingDto approveBooking(Long bookingId);

    BookingDto completeBooking(Long bookingId);

    BookingDto cancelBooking(Long bookingId);

    BookingDto raiseDispute(Long bookingId, String disputeDescription);

    BookingDto resolveDispute(Long bookingId);

    BookingDto updateBookingStatus(Long bookingId, BookingStatusUpdateRequestDto updateDto);

    List<Booking> findBookingsByRenterId(Long renterId);

    List<Booking> searchBookings(BookingSearchCriteria criteria);
}