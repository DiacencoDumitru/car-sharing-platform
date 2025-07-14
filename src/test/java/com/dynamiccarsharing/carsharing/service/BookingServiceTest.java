package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.exception.BookingNotFoundException;
import com.dynamiccarsharing.carsharing.exception.InvalidBookingStatusException;
import com.dynamiccarsharing.carsharing.exception.InvalidDisputeStatusException;
import com.dynamiccarsharing.carsharing.model.*;
import com.dynamiccarsharing.carsharing.repository.BookingRepository;
import com.dynamiccarsharing.carsharing.repository.DisputeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private DisputeRepository disputeRepository;

    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(bookingRepository, disputeRepository);
    }

    private Booking createTestBooking(UUID id, TransactionStatus status) {
        return Booking.builder()
                .id(id)
                .renter(User.builder().id(UUID.randomUUID()).build())
                .car(Car.builder().id(UUID.randomUUID()).build())
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .status(status)
                .pickupLocation(Location.builder().id(UUID.randomUUID()).build())
                .build();
    }

    @Test
    void approveBooking_withPendingStatus_shouldSucceed() {
        UUID bookingId = UUID.randomUUID();
        Booking pendingBooking = createTestBooking(bookingId, TransactionStatus.PENDING);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(pendingBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking approvedBooking = bookingService.approveBooking(bookingId);

        assertNotNull(approvedBooking);
        assertEquals(TransactionStatus.APPROVED, approvedBooking.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void approveBooking_withCompletedStatus_shouldThrowInvalidBookingStatusException() {
        UUID bookingId = UUID.randomUUID();
        Booking completedBooking = createTestBooking(bookingId, TransactionStatus.COMPLETED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(completedBooking));

        assertThrows(InvalidBookingStatusException.class, () -> {
            bookingService.approveBooking(bookingId);
        });
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void raiseDispute_withCompletedBooking_shouldWork() {
        UUID bookingId = UUID.randomUUID();
        Booking completedBooking = createTestBooking(bookingId, TransactionStatus.COMPLETED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(completedBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = bookingService.raiseDispute(bookingId, "Test dispute");

        verify(disputeRepository, times(1)).save(any(Dispute.class));
        assertEquals(DisputeStatus.OPEN, result.getDisputeStatus());
    }

    @Test
    void resolveDispute_withNonOpenDispute_shouldThrowInvalidDisputeStatusException() {
        UUID bookingId = UUID.randomUUID();
        Booking bookingWithResolvedDispute = createTestBooking(bookingId, TransactionStatus.COMPLETED).withDisputeStatus(DisputeStatus.RESOLVED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(bookingWithResolvedDispute));

        assertThrows(InvalidDisputeStatusException.class, () -> {
            bookingService.resolveDispute(bookingId);
        });
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void findBookingsByRenterId_shouldCallRepository() {
        UUID renterId = UUID.randomUUID();
        when(bookingRepository.findByRenterId(renterId)).thenReturn(List.of(createTestBooking(UUID.randomUUID(), TransactionStatus.PENDING)));

        List<Booking> results = bookingService.findBookingsByRenterId(renterId);

        assertFalse(results.isEmpty());
        verify(bookingRepository, times(1)).findByRenterId(renterId);
    }

    @Test
    void searchBookings_shouldCallRepositoryWithSpecification() {
        UUID carId = UUID.randomUUID();
        TransactionStatus status = TransactionStatus.PENDING;
        when(bookingRepository.findAll(any(Specification.class))).thenReturn(List.of(createTestBooking(UUID.randomUUID(), status)));

        List<Booking> results = bookingService.searchBookings(null, carId, status);

        assertFalse(results.isEmpty());
        verify(bookingRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void getBookingOrThrow_whenNotFound_shouldThrowBookingNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();
        when(bookingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> {
            bookingService.getBookingOrThrow(nonExistentId);
        });
    }
}