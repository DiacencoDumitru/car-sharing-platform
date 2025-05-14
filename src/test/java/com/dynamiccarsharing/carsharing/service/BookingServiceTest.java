package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.repository.BookingRepository;
import com.dynamiccarsharing.carsharing.repository.DisputeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    BookingRepository bookingRepository;

    @Mock
    DisputeRepository disputeRepository;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reset(bookingRepository);
        bookingService = new BookingService(bookingRepository, disputeRepository);
    }

    private Booking createTestBooking(TransactionStatus status, String disputeDescription, DisputeStatus disputeStatus) {
        return new Booking(1L, 2L, 3L, LocalDateTime.now(), LocalDateTime.now().plusHours(2), status, new Location(1L, "New York", "New York", "10001"), disputeDescription, disputeStatus);
    }

    @Test
    void save_shouldCallRepository_shouldReturnSameBooking() {
        Booking booking = createTestBooking(TransactionStatus.PENDING, null, null);

        Booking savedBooking = bookingService.save(booking);

        verify(bookingRepository, times(1)).save(booking);
        assertEquals(booking, savedBooking);
    }

    @Test
    void save_whenBookingIsNull_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> bookingService.save(null));
    }

    @Test
    void findById_whenBookingIsPresent_shouldReturnBooking() {
        Booking booking = createTestBooking(TransactionStatus.PENDING, null, null);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Optional<Booking> foundBooking = bookingService.findById(1L);

        verify(bookingRepository, times(1)).findById(1L);
        assertTrue(foundBooking.isPresent());
        assertEquals(booking, foundBooking.get());
        assertEquals(TransactionStatus.PENDING, foundBooking.get().getStatus());
    }

    @Test
    void findById_whenBookingNotFound_shouldReturnEmpty() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Booking> foundBook = bookingService.findById(1L);

        verify(bookingRepository, times(1)).findById(1L);
        assertFalse(foundBook.isPresent());
    }

    @Test
    void findById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> bookingService.findById(-1L));

        assertEquals("Booking ID must be non-negative", exception.getMessage());
        verify(bookingRepository, never()).findById(any());
    }

    @Test
    void deleteById_withValidId_shouldDeleteBooking() {
        bookingService.deleteById(1L);

        verify(bookingRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_withInvalidId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> bookingService.deleteById(-1L));

        assertEquals("Booking ID must be non-negative", exception.getMessage());
        verify(bookingRepository, never()).findById(any());
    }

    @Test
    void findAll_withMultipleBookings_shouldReturnAllBookings() {
        Booking booking1 = createTestBooking(TransactionStatus.PENDING, null, null);
        Booking booking2 = new Booking(2L, 3L, 4L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), TransactionStatus.APPROVED, new Location(2L, "Chisinau", "Chisinau", "1001"), null, null);
        List<Booking> bookings = Arrays.asList(booking1, booking2);
        when(bookingRepository.findAll()).thenReturn(bookings);

        Iterable<Booking> result = bookingService.findAll();

        verify(bookingRepository, times(1)).findAll();
        assertEquals(bookings, result);
        List<Booking> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertIterableEquals(bookings, result);
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(booking1));
        assertTrue(resultList.contains(booking2));
    }

    @Test
    void findAll_withSingleBooking_shouldReturnSingleBooking() {
        Booking booking = createTestBooking(TransactionStatus.PENDING, null, null);
        List<Booking> bookings = Collections.singletonList(booking);
        when(bookingRepository.findAll()).thenReturn(bookings);

        Iterable<Booking> result = bookingService.findAll();

        verify(bookingRepository, times(1)).findAll();
        assertEquals(bookings, result);
        List<Booking> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertIterableEquals(bookings, result);
        assertEquals(1, resultList.size());
        assertEquals(booking, resultList.get(0));
    }

    @Test
    void findAll_withNoBookings_shouldReturnEmptyIterable() {
        List<Booking> bookings = Collections.emptyList();
        when(bookingRepository.findAll()).thenReturn(bookings);

        Iterable<Booking> result = bookingService.findAll();

        verify(bookingRepository, times(1)).findAll();
        assertEquals(bookings, result);
        List<Booking> resultList = StreamSupport.stream(result.spliterator(), false).toList();
        assertIterableEquals(bookings, result);
        assertEquals(0, resultList.size());
    }

    @Test
    void approveBooking_withPendingStatus_shouldApprove() {
        Booking booking = createTestBooking(TransactionStatus.PENDING, null, null);
        Booking approvedBooking = booking.withStatus(TransactionStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(approvedBooking);

        Booking result = bookingService.approveBooking(1L);

        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).save(approvedBooking);
        assertEquals(TransactionStatus.APPROVED, result.getStatus());
    }

    @Test
    void completeBooking_withApprovedStatus_shouldComplete() {
        Booking booking = createTestBooking(TransactionStatus.APPROVED, null, null);
        Booking completedBooking = booking.withStatus(TransactionStatus.COMPLETED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(completedBooking);

        Booking result = bookingService.completeBooking(1L);

        verify(bookingRepository, times(1)).findById(1L);
        ;
        verify(bookingRepository, times(1)).save(completedBooking);
        ;
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
    }

    @Test
    void cancelBooking_withPendingStatus_shouldCancel() {
        Booking booking = createTestBooking(TransactionStatus.PENDING, null, null);
        Booking cancelBooking = booking.withStatus(TransactionStatus.CANCELED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(cancelBooking);

        Booking result = bookingService.cancelBooking(1L);

        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).save(cancelBooking);
        assertEquals(TransactionStatus.CANCELED, result.getStatus());
    }

    @Test
    void raiseDispute_withCompletedStatusAndValidDescription_shouldRaiseDispute() {
        Booking booking = createTestBooking(TransactionStatus.COMPLETED, null, null);
        Booking updatedBooking = booking
                .withDisputeDescription("Problem description")
                .withDisputeStatus(DisputeStatus.OPEN);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        doReturn(updatedBooking).when(bookingRepository).save(updatedBooking);

        Booking result = bookingService.raiseDispute(1L, "Problem description");

        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).save(updatedBooking);
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        assertEquals(DisputeStatus.OPEN, result.getDisputeStatus());
        assertEquals("Problem description", result.getDisputeDescription());
    }

    @Test
    void raiseDispute_withEmptyDescription_shouldThrowException() {
        Booking booking = createTestBooking(TransactionStatus.COMPLETED, null, null);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> bookingService.raiseDispute(1L, ""));

        assertEquals("Description must be non-empty if provided", exception.getMessage());
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void resolveDispute_withOpenStatus_shouldResolve() {
        Booking booking = createTestBooking(TransactionStatus.COMPLETED, "Problem description", DisputeStatus.OPEN);
        Booking resolvedBooking = booking.withDisputeStatus(DisputeStatus.RESOLVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(resolvedBooking);

        Booking result = bookingService.resolveDispute(1L);

        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).save(resolvedBooking);
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        assertEquals(DisputeStatus.RESOLVED, result.getDisputeStatus());
    }

    @Test
    void validateBookingStatus_withInvalidStatus_shouldThrowException() {
        Booking booking = createTestBooking(TransactionStatus.APPROVED, null, null);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> bookingService.approveBooking(1L));

        assertEquals("Booking can only be approved from PENDING status", exception.getMessage());
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void validateDisputeStatus_withInvalidStatus_shouldThrowException() {
        Booking booking = createTestBooking(TransactionStatus.COMPLETED, "Problem description", DisputeStatus.RESOLVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> bookingService.resolveDispute(1L));

        assertEquals("Can only resolve an open dispute", exception.getMessage());
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void findBookingsByRenterId_withValidRenterId_shouldReturnBookings() {
        Booking booking = createTestBooking(TransactionStatus.COMPLETED, null, null);
        List<Booking> bookings = List.of(booking);
        when(bookingRepository.findByFilter(argThat(filter -> filter != null && filter.test(booking) && booking.getRenterId().equals(2L)))).thenReturn(bookings);

        List<Booking> result = bookingService.findBookingsByRenterId(2L);

        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
        assertEquals(booking.getCarId(), result.get(0).getCarId());
        assertEquals(booking.getRenterId(), result.get(0).getRenterId());
        assertEquals(booking.getStatus(), result.get(0).getStatus());
        verify(bookingRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(booking) && booking.getRenterId().equals(2L)));
    }

    @Test
    void findBookingsByRenterId_withInvalidRenterId_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> bookingService.findBookingsByRenterId(-1L));
        assertEquals("Renter ID must be non-negative", exception.getMessage());
        verify(bookingRepository, never()).findById(any());
    }

    @Test
    void findBookingsByCarId_withValidCarId_shouldReturnBookings() {
        Booking booking = createTestBooking(TransactionStatus.COMPLETED, null, null);
        List<Booking> bookings = List.of(booking);
        when(bookingRepository.findByFilter(argThat(filter -> filter != null && filter.test(booking) && booking.getCarId().equals(3L)))).thenReturn(bookings);

        List<Booking> result = bookingService.findBookingsByCarId(3L);

        assertEquals(bookings, result);
        assertEquals(1, result.size());
        verify(bookingRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(booking) && booking.getCarId().equals(3L)));
    }

    @Test
    void findBookingsByStatus_withStatus_shouldReturnBookings() {
        Booking booking = createTestBooking(TransactionStatus.COMPLETED, null, null);
        List<Booking> bookings = List.of(booking);
        when(bookingRepository.findByFilter(argThat(filter -> filter != null && filter.test(booking) && booking.getStatus().equals(TransactionStatus.COMPLETED)))).thenReturn(bookings);

        List<Booking> result = bookingService.findBookingsByStatus(TransactionStatus.COMPLETED);

        assertEquals(bookings, result);
        assertEquals(1, result.size());
        verify(bookingRepository, times(1)).findByFilter(argThat(filter -> filter != null && filter.test(booking) && booking.getStatus().equals(TransactionStatus.COMPLETED)));
    }
}