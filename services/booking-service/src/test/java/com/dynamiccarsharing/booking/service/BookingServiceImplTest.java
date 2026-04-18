package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.booking.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.booking.dto.PaymentDto;
import com.dynamiccarsharing.booking.mapper.BookingMapper;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.messaging.outbox.BookingLifecycleOutboxWriter;
import com.dynamiccarsharing.booking.integration.client.CarIntegrationClient;
import com.dynamiccarsharing.booking.integration.client.UserIntegrationClient;
import com.dynamiccarsharing.booking.service.interfaces.PaymentService;
import com.dynamiccarsharing.booking.service.interfaces.BookingCreationGuard;
import com.dynamiccarsharing.contracts.dto.BookingDto;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private PaymentService paymentService;
    @Mock
    private BookingCreationGuard bookingCreationGuard;
    @Mock
    private BookingLifecycleOutboxWriter bookingLifecycleOutboxWriter;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private UserIntegrationClient userIntegrationClient;
    @Mock
    private CarIntegrationClient carIntegrationClient;

    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(
                bookingRepository,
                bookingMapper,
                paymentService,
                bookingCreationGuard,
                bookingLifecycleOutboxWriter,
                applicationEventPublisher,
                userIntegrationClient,
                carIntegrationClient
        );

        lenient().when(bookingCreationGuard.executeWithCarLock(any(), any())).thenAnswer(invocation -> {
            java.util.function.Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });

    }

    private BookingCreateRequestDto createTestBookingDto() {
        BookingCreateRequestDto dto = new BookingCreateRequestDto();
        dto.setRenterId(100L);
        dto.setCarId(200L);
        return dto;
    }

    private Booking createTestBooking(Long id, TransactionStatus status) {
        return Booking.builder()
                .id(id)
                .renterId(100L)
                .carId(200L)
                .pickupLocationId(300L)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .status(status)
                .build();
    }

    @Test
    @DisplayName("save() should create booking when user and car are valid")
    void save_whenValid_shouldMapAndReturnDto() {
        BookingCreateRequestDto createDto = createTestBookingDto();
        Booking bookingEntity = createTestBooking(null, TransactionStatus.PENDING);
        Booking savedBookingEntity = createTestBooking(1L, TransactionStatus.PENDING);
        BookingDto expectedDto = new BookingDto();
        expectedDto.setId(1L);

        doNothing().when(userIntegrationClient).assertUserExists(createDto.getRenterId());
        doNothing().when(carIntegrationClient).assertCarAvailable(createDto.getCarId());

        when(bookingMapper.toEntity(createDto)).thenReturn(bookingEntity);
        when(bookingRepository.hasOverlappingBooking(any(), any(), any())).thenReturn(false);
        when(bookingRepository.save(bookingEntity)).thenReturn(savedBookingEntity);
        when(bookingMapper.toDto(savedBookingEntity)).thenReturn(expectedDto);

        BookingDto resultDto = bookingService.save(createDto);

        assertNotNull(resultDto);
        assertEquals(1L, resultDto.getId());

        verify(userIntegrationClient).assertUserExists(createDto.getRenterId());
        verify(carIntegrationClient).assertCarAvailable(createDto.getCarId());
    }

    @Test
    @DisplayName("save() should throw exception when user does not exist")
    void save_whenUserNotFound_shouldThrowException() {
        BookingCreateRequestDto createDto = createTestBookingDto();
        doThrow(new RuntimeException("user not found")).when(userIntegrationClient).assertUserExists(createDto.getRenterId());
        assertThrows(RuntimeException.class, () -> bookingService.save(createDto));
    }

    @Test
    @DisplayName("save() should throw exception when car is not available")
    void save_whenCarNotAvailable_shouldThrowException() {
        BookingCreateRequestDto createDto = createTestBookingDto();
        doNothing().when(userIntegrationClient).assertUserExists(createDto.getRenterId());
        doThrow(new RuntimeException("car unavailable")).when(carIntegrationClient).assertCarAvailable(createDto.getCarId());
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
        Booking approvedBooking = createTestBooking(bookingId, TransactionStatus.APPROVED);
        PaymentDto completedPayment = new PaymentDto();
        completedPayment.setStatus(TransactionStatus.COMPLETED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(approvedBooking));
        when(paymentService.findByBookingId(bookingId)).thenReturn(Optional.of(completedPayment));
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        when(bookingRepository.save(bookingCaptor.capture())).thenReturn(new Booking());

        assertDoesNotThrow(() -> bookingService.completeBooking(bookingId));

        assertEquals(TransactionStatus.COMPLETED, bookingCaptor.getValue().getStatus());
    }

    @Test
    void cancelBooking_withPendingStatus_shouldSucceed() {
        Long bookingId = 1L;
        Booking pendingBooking = createTestBooking(bookingId, TransactionStatus.PENDING);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(pendingBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(new Booking());

        assertDoesNotThrow(() -> bookingService.cancelBooking(bookingId));
    }

}