package com.dynamiccarsharing.carsharing.service.jpa;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.exception.BookingNotFoundException;
import com.dynamiccarsharing.carsharing.exception.InvalidBookingStatusException;
import com.dynamiccarsharing.carsharing.exception.InvalidDisputeStatusException;
import com.dynamiccarsharing.carsharing.model.*;
import com.dynamiccarsharing.carsharing.repository.jpa.BookingJpaRepository;
import com.dynamiccarsharing.carsharing.repository.jpa.DisputeJpaRepository;
import com.dynamiccarsharing.carsharing.dto.BookingSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceJpaTest {

    @Mock
    private BookingJpaRepository bookingRepository;

    @Mock
    private DisputeJpaRepository disputeRepository;

    private BookingServiceJpaImpl bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceJpaImpl(bookingRepository, disputeRepository);
    }

    private Booking createTestBooking(Long id, TransactionStatus status) {
        return Booking.builder()
                .id(id)
                .renter(User.builder().id(1L).build())
                .car(Car.builder().id(1L).build())
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .status(status)
                .pickupLocation(Location.builder().id(1L).build())
                .build();
    }

    @Test
    void approveBooking_withPendingStatus_shouldSucceed() {
        Long bookingId = 1L;
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
        Long bookingId = 1L;
        Booking completedBooking = createTestBooking(bookingId, TransactionStatus.COMPLETED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(completedBooking));

        assertThrows(InvalidBookingStatusException.class, () -> bookingService.approveBooking(bookingId));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void approveBooking_whenNotFound_shouldThrowBookingNotFoundException() {
        Long nonExistentId = 99L;
        when(bookingRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        assertThrows(BookingNotFoundException.class, () -> bookingService.approveBooking(nonExistentId));
    }

    @Test
    void raiseDispute_withCompletedBooking_shouldWork() {
        Long bookingId = 1L;
        Booking completedBooking = createTestBooking(bookingId, TransactionStatus.COMPLETED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(completedBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = bookingService.raiseDispute(bookingId, "Test dispute");

        verify(disputeRepository, times(1)).save(any(Dispute.class));
        assertEquals(DisputeStatus.OPEN, result.getDisputeStatus());
    }

    @Test
    void resolveDispute_withNonOpenDispute_shouldThrowInvalidDisputeStatusException() {
        Long bookingId = 1L;
        Booking bookingWithResolvedDispute = createTestBooking(bookingId, TransactionStatus.COMPLETED).withDisputeStatus(DisputeStatus.RESOLVED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(bookingWithResolvedDispute));

        assertThrows(InvalidDisputeStatusException.class, () -> bookingService.resolveDispute(bookingId));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void findBookingsByRenterId_shouldCallRepository() {
        Long renterId = 1L;
        when(bookingRepository.findByRenterId(renterId)).thenReturn(List.of(createTestBooking(1L, TransactionStatus.PENDING)));

        List<Booking> results = bookingService.findBookingsByRenterId(renterId);

        assertFalse(results.isEmpty());
        verify(bookingRepository, times(1)).findByRenterId(renterId);
    }

    @Test
    void searchBookings_shouldCallRepositoryWithSpecification() {
        Long carId = 2L;
        TransactionStatus status = TransactionStatus.PENDING;
        BookingSearchCriteria criteria = BookingSearchCriteria.builder().carId(carId).status(status).build();
        when(bookingRepository.findAll(any(Specification.class))).thenReturn(List.of(createTestBooking(1L, status)));

        List<Booking> results = bookingService.searchBookings(criteria);

        assertFalse(results.isEmpty());
        verify(bookingRepository, times(1)).findAll(any(Specification.class));
    }
}