package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.InMemoryBookingRepository;
import com.dynamiccarsharing.carsharing.util.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    InMemoryBookingRepository inMemoryBookingRepository;

    @Test
    void save_shouldCallRepository_shouldReturnSameBooking() {
        Location location = new Location(1L, "New", "York", "999");
        Booking booking = new Booking(
                1L,
                2L,
                3L,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                TransactionStatus.PENDING,
                location,
                null,
                null
        );
        BookingService bookingService = new BookingService(inMemoryBookingRepository);

        Booking savedBooking = bookingService.save(booking);

        verify(inMemoryBookingRepository, times(1)).save(booking);
        assertEquals(booking, savedBooking);
    }

    @Test
    void save_whenBookingIsNull_shouldThrowException() {
        BookingService bookingService = new BookingService(inMemoryBookingRepository);
        assertThrows(IllegalArgumentException.class, () -> bookingService.save(null));
    }

    @Test
    void findById() {
        BookingService service = new BookingService(inMemoryBookingRepository);

        Booking booking = mock(Booking.class);
        when(inMemoryBookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Optional<Booking> foundBook = service.findById(1L);

        verify(inMemoryBookingRepository, times(1)).findById(1L);
        assertTrue(foundBook.isPresent());
        assertEquals(booking, foundBook.get());
    }

    @Test
    void delete() {
    }

    @Test
    void findAll() {
    }

    @Test
    void approveBooking() {
    }

    @Test
    void completeBooking() {
    }

    @Test
    void cancelBooking() {
    }

    @Test
    void raiseDispute() {
    }

    @Test
    void resolveDispute() {
    }

    @Test
    void findBookingsByRenterId() {
    }
}