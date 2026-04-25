package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.application.usecase.BookingCreationUseCase;
import com.dynamiccarsharing.booking.application.usecase.BookingStatusUseCase;
import com.dynamiccarsharing.booking.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.booking.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.booking.integration.client.CarIntegrationClient;
import com.dynamiccarsharing.booking.mapper.BookingMapper;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.contracts.dto.BookingDto;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private BookingCreationUseCase bookingCreationUseCase;
    @Mock
    private BookingStatusUseCase bookingStatusUseCase;
    @Mock
    private CarIntegrationClient carIntegrationClient;

    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(
                bookingRepository,
                bookingMapper,
                bookingCreationUseCase,
                bookingStatusUseCase,
                carIntegrationClient
        );
    }

    private BookingCreateRequestDto createTestBookingDto() {
        BookingCreateRequestDto dto = new BookingCreateRequestDto();
        dto.setRenterId(100L);
        dto.setCarId(200L);
        return dto;
    }

    @Test
    @DisplayName("save() delegates booking creation use case")
    void save_whenValid_shouldMapAndReturnDto() {
        BookingCreateRequestDto createDto = createTestBookingDto();
        BookingDto expectedDto = new BookingDto();
        expectedDto.setId(1L);
        when(bookingCreationUseCase.createBooking(createDto)).thenReturn(expectedDto);

        BookingDto resultDto = bookingService.save(createDto);

        assertNotNull(resultDto);
        assertEquals(1L, resultDto.getId());
        verify(bookingCreationUseCase).createBooking(createDto);
    }

    @Test
    @DisplayName("save() should propagate use case exception")
    void save_whenUserNotFound_shouldThrowException() {
        BookingCreateRequestDto createDto = createTestBookingDto();
        doThrow(new RuntimeException("user not found")).when(bookingCreationUseCase).createBooking(createDto);
        assertThrows(RuntimeException.class, () -> bookingService.save(createDto));
    }

    @Test
    @DisplayName("save() should propagate use case exception")
    void save_whenCarNotAvailable_shouldThrowException() {
        BookingCreateRequestDto createDto = createTestBookingDto();
        doThrow(new RuntimeException("car unavailable")).when(bookingCreationUseCase).createBooking(createDto);
        assertThrows(RuntimeException.class, () -> bookingService.save(createDto));
    }

    @Test
    void findAll_withCriteria_shouldCallRepositoryAndMapper() {
        BookingSearchCriteria criteria = new BookingSearchCriteria();
        Pageable pageable = PageRequest.of(0, 10);
        Booking booking = new Booking();
        Page<Booking> bookingPage = new PageImpl<>(List.of(booking));
        BookingDto bookingDto = new BookingDto();

        when(bookingRepository.findAll(criteria, pageable)).thenReturn(bookingPage);
        when(bookingMapper.toDto(any(Booking.class))).thenReturn(bookingDto);

        Page<BookingDto> resultPage = bookingService.findAll(criteria, pageable);

        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        verify(bookingRepository).findAll(criteria, pageable);
        verify(bookingMapper).toDto(booking);
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
    void completeBooking_withApprovedStatusAndCompletedPayment_shouldSucceed() {
        Long bookingId = 1L;
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStatus(TransactionStatus.COMPLETED);
        when(bookingStatusUseCase.completeBooking(bookingId)).thenReturn(bookingDto);

        assertDoesNotThrow(() -> bookingService.completeBooking(bookingId));
        verify(bookingStatusUseCase).completeBooking(bookingId);
    }

    @Test
    void cancelBooking_withPendingStatus_shouldSucceed() {
        Long bookingId = 1L;
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStatus(TransactionStatus.CANCELED);
        when(bookingStatusUseCase.cancelBooking(bookingId)).thenReturn(bookingDto);

        assertDoesNotThrow(() -> bookingService.cancelBooking(bookingId));
        verify(bookingStatusUseCase).cancelBooking(bookingId);
    }
}