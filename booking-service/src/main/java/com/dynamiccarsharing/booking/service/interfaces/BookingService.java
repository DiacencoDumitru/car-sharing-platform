package com.dynamiccarsharing.booking.service.interfaces;

import com.dynamiccarsharing.booking.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.booking.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.booking.dto.BookingStatusUpdateRequestDto;
import com.dynamiccarsharing.contracts.dto.BookingDto;
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

    BookingDto updateBookingStatus(Long bookingId, BookingStatusUpdateRequestDto updateDto);

    List<BookingDto> findBookingsByRenterId(Long renterId);

    List<BookingDto> searchBookings(BookingSearchCriteria criteria);
}