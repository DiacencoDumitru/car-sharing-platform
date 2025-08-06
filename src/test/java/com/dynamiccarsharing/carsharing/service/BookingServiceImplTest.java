package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.carsharing.dto.BookingDto;
import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.exception.InvalidDisputeStatusException;
import com.dynamiccarsharing.carsharing.mapper.BookingMapper;
import com.dynamiccarsharing.carsharing.model.Booking;
import com.dynamiccarsharing.carsharing.model.Car;
import com.dynamiccarsharing.carsharing.model.Location;
import com.dynamiccarsharing.carsharing.model.User;
import com.dynamiccarsharing.carsharing.repository.jpa.BookingJpaRepository;
import com.dynamiccarsharing.carsharing.repository.jpa.DisputeJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingJpaRepository bookingRepository;

    @Mock
    private DisputeJpaRepository disputeRepository;

    @Mock
    private BookingMapper bookingMapper;


    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, disputeRepository, bookingMapper);
    }

    private Booking createTestBooking(Long id, TransactionStatus status, DisputeStatus disputeStatus) {
        return Booking.builder()
                .id(id)
                .renter(User.builder().id(1L).build())
                .car(Car.builder().id(1L).build())
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .status(status)
                .disputeStatus(disputeStatus)
                .pickupLocation(Location.builder().id(1L).build())
                .build();
    }

    @Test
    void save_shouldMapAndReturnDto() {
        BookingCreateRequestDto createDto = new BookingCreateRequestDto();
        Booking bookingEntity = createTestBooking(null, TransactionStatus.PENDING, null);
        Booking savedBookingEntity = createTestBooking(1L, TransactionStatus.PENDING, null);
        BookingDto expectedDto = new BookingDto();
        expectedDto.setId(1L);

        when(bookingMapper.toEntity(createDto)).thenReturn(bookingEntity);
        when(bookingRepository.save(bookingEntity)).thenReturn(savedBookingEntity);
        when(bookingMapper.toDto(savedBookingEntity)).thenReturn(expectedDto);

        BookingDto resultDto = bookingService.save(createDto);

        assertNotNull(resultDto);
        assertEquals(1L, resultDto.getId());
    }

    @Test
    void deleteById_whenBookingExists_shouldCallRepositoryDelete() {
        Long bookingId = 1L;
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(new Booking()));
        doNothing().when(bookingRepository).deleteById(bookingId);

        bookingService.deleteById(bookingId);

        verify(bookingRepository).deleteById(bookingId);
    }

    @Test
    void completeBooking_withApprovedStatus_shouldSucceed() {
        Long bookingId = 1L;
        Booking approvedBooking = createTestBooking(bookingId, TransactionStatus.APPROVED, null);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(approvedBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(new Booking());

        assertDoesNotThrow(() -> bookingService.completeBooking(bookingId));
    }

    @Test
    void cancelBooking_withPendingStatus_shouldSucceed() {
        Long bookingId = 1L;
        Booking pendingBooking = createTestBooking(bookingId, TransactionStatus.PENDING, null);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(pendingBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(new Booking());

        assertDoesNotThrow(() -> bookingService.cancelBooking(bookingId));
    }

    @Test
    void raiseDispute_withNonCompletedBooking_shouldThrowException() {
        Long bookingId = 1L;
        Booking pendingBooking = createTestBooking(bookingId, TransactionStatus.PENDING, null);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(pendingBooking));

        assertThrows(IllegalStateException.class, () -> bookingService.raiseDispute(bookingId, "Test"));
    }

    @Test
    void resolveDispute_withOpenDispute_shouldSucceed() {
        Long bookingId = 1L;
        Booking openDisputeBooking = createTestBooking(bookingId, TransactionStatus.COMPLETED, DisputeStatus.OPEN);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(openDisputeBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(new Booking());

        assertDoesNotThrow(() -> bookingService.resolveDispute(bookingId));
    }

    @Test
    void resolveDispute_withResolvedDispute_shouldThrowException() {
        Long bookingId = 1L;
        Booking resolvedDisputeBooking = createTestBooking(bookingId, TransactionStatus.COMPLETED, DisputeStatus.RESOLVED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(resolvedDisputeBooking));

        assertThrows(InvalidDisputeStatusException.class, () -> bookingService.resolveDispute(bookingId));
    }
}