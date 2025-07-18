package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.filter.BookingFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InMemoryBookingRepositoryTest {

    private InMemoryBookingRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryBookingRepository();
        repository.findAll().forEach(booking -> repository.deleteById(booking.getId()));
    }

    private Booking createTestBooking(Long id, TransactionStatus status) {
        LocalDateTime now = LocalDateTime.now();
        Location location = new Location(1L, "New York", "NY", "10001");
        return new Booking(id, 2L, 3L, now, now.plusHours(2), status, location, null, null);
    }

    @Test
    void save_shouldSaveAndReturnBooking() {
        Booking booking = createTestBooking(1L, TransactionStatus.PENDING);

        Booking savedBooking = repository.save(booking);

        assertEquals(booking, savedBooking);
        assertTrue(repository.findById(1L).isPresent());
        assertEquals(booking, repository.findById(1L).get());
    }

    @Test
    void save_withNullBooking_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> repository.save(null));
    }

    @Test
    void findById_withExistingId_shouldReturnBooking() {
        Booking booking = createTestBooking(1L, TransactionStatus.PENDING);
        repository.save(booking);

        Optional<Booking> foundBooking = repository.findById(1L);

        assertTrue(foundBooking.isPresent());
        assertEquals(booking, foundBooking.get());
    }

    @Test
    void findById_withNonExistingId_shouldReturnEmpty() {
        Optional<Booking> foundBooking = repository.findById(1L);

        assertFalse(foundBooking.isPresent());
    }

    @Test
    void deleteById_withExistingId_shouldRemoveBooking() {
        Booking booking = createTestBooking(1L, TransactionStatus.PENDING);
        repository.save(booking);

        repository.deleteById(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void deleteById_withNonExistingId_shouldDoNothing() {
        repository.deleteById(1L);

        assertFalse(repository.findById(1L).isPresent());
    }

    @Test
    void findAll_withMultipleBookings_shouldReturnAllBookings() {
        Booking booking1 = createTestBooking(1L, TransactionStatus.PENDING);
        Booking booking2 = createTestBooking(2L, TransactionStatus.APPROVED);
        repository.save(booking1);
        repository.save(booking2);

        Iterable<Booking> bookings = repository.findAll();
        List<Booking> bookingList = new ArrayList<>();
        bookings.forEach(bookingList::add);

        assertEquals(2, bookingList.size());
        assertTrue(bookingList.contains(booking1));
        assertTrue(bookingList.contains(booking2));
    }

    @Test
    void findAll_withSingleBooking_shouldReturnSingleBooking() {
        Booking booking = createTestBooking(1L, TransactionStatus.PENDING);
        repository.save(booking);

        Iterable<Booking> bookings = repository.findAll();
        List<Booking> bookingList = new ArrayList<>();
        bookings.forEach(bookingList::add);

        assertEquals(1, bookingList.size());
        assertEquals(booking, bookingList.get(0));
    }

    @Test
    void findAll_withNoBookings_shouldReturnEmptyIterable() {
        Iterable<Booking> bookings = repository.findAll();
        List<Booking> bookingList = new ArrayList<>();
        bookings.forEach(bookingList::add);

        assertEquals(0, bookingList.size());
    }

    @Test
    void findByFilter_withMatchingBookings_shouldReturnMatchingBookings() {
        Booking booking1 = createTestBooking(1L, TransactionStatus.PENDING);
        Booking booking2 = createTestBooking(2L, TransactionStatus.APPROVED);
        Booking booking3 = createTestBooking(3L, TransactionStatus.PENDING);
        repository.save(booking1);
        repository.save(booking2);
        repository.save(booking3);
        BookingFilter filter = mock(BookingFilter.class);
        when(filter.test(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            return booking.getStatus() == TransactionStatus.PENDING;
        });

        List<Booking> filteredBookings = repository.findByFilter(filter);

        assertEquals(2, filteredBookings.size());
        assertTrue(filteredBookings.contains(booking1));
        assertTrue(filteredBookings.contains(booking3));
        assertFalse(filteredBookings.contains(booking2));
    }

    @Test
    void findByFilter_withNoMatchingBookings_shouldReturnEmptyList() {
        Booking booking = createTestBooking(1L, TransactionStatus.PENDING);
        repository.save(booking);
        BookingFilter filter = mock(BookingFilter.class);
        when(filter.test(any(Booking.class))).thenReturn(false);

        List<Booking> filteredBookings = repository.findByFilter(filter);

        assertEquals(0, filteredBookings.size());
    }
}