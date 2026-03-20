package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.criteria.BookingSearchCriteria;
import com.dynamiccarsharing.booking.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.booking.dto.PaymentDto;
import com.dynamiccarsharing.booking.mapper.BookingMapper;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.service.interfaces.PaymentService;
import com.dynamiccarsharing.booking.service.interfaces.BookingCreationGuard;
import com.dynamiccarsharing.contracts.dto.BookingDto;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.util.exception.ValidationException;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

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
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private WebClient.Builder webClientBuilder;
    @Mock
    private WebClient userWebClient;
    @Mock
    private WebClient carWebClient;

    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(
                bookingRepository,
                bookingMapper,
                paymentService,
                bookingCreationGuard,
                applicationEventPublisher,
                webClientBuilder
        );

        // Только save() вызывает guard; lenient — чтобы не падать UnnecessaryStubbing в остальных тестах
        lenient().when(bookingCreationGuard.executeWithCarLock(any(), any())).thenAnswer(invocation -> {
            java.util.function.Supplier<?> supplier = invocation.getArgument(1);
            return supplier.get();
        });

        ReflectionTestUtils.setField(bookingService, "userWebClient", userWebClient);
        ReflectionTestUtils.setField(bookingService, "carWebClient", carWebClient);
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

        mockUserWebClient(createDto.getRenterId(), new UserDto());

        CarDto availableCar = new CarDto();
        availableCar.setStatus(CarStatus.AVAILABLE);
        mockCarWebClient(createDto.getCarId(), availableCar);

        when(bookingMapper.toEntity(createDto)).thenReturn(bookingEntity);
        when(bookingRepository.hasOverlappingBooking(any(), any(), any())).thenReturn(false);
        when(bookingRepository.save(bookingEntity)).thenReturn(savedBookingEntity);
        when(bookingMapper.toDto(savedBookingEntity)).thenReturn(expectedDto);

        BookingDto resultDto = bookingService.save(createDto);

        assertNotNull(resultDto);
        assertEquals(1L, resultDto.getId());

        verify(userWebClient.get()).uri("/api/v1/users/" + createDto.getRenterId());

        verify(carWebClient.get()).uri("/api/v1/cars/" + createDto.getCarId());
    }

    @Test
    @DisplayName("save() should throw exception when user does not exist")
    void save_whenUserNotFound_shouldThrowException() {
        BookingCreateRequestDto createDto = createTestBookingDto();
        mockUserWebClient(createDto.getRenterId(), null);

        assertThrows(ValidationException.class, () -> bookingService.save(createDto));
    }

    @Test
    @DisplayName("save() should throw exception when car is not available")
    void save_whenCarNotAvailable_shouldThrowException() {
        BookingCreateRequestDto createDto = createTestBookingDto();
        mockUserWebClient(createDto.getRenterId(), new UserDto());

        CarDto rentedCar = new CarDto();
        rentedCar.setStatus(CarStatus.RENTED);
        mockCarWebClient(createDto.getCarId(), rentedCar);

        assertThrows(ValidationException.class, () -> bookingService.save(createDto));
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

    private void mockUserWebClient(Long userId, UserDto responseDto) {
        var requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        Mono<UserDto> mono = mock(Mono.class);

        String expectedUri = "/api/v1/users/" + userId;

        when(userWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(expectedUri)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(mono);

        if (responseDto != null) {
            when(mono.block()).thenReturn(responseDto);
        } else {
            when(mono.block()).thenThrow(new RuntimeException("Simulated 404 Not Found"));
        }
    }

    private void mockCarWebClient(Long carId, CarDto responseDto) {
        var requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        Mono<CarDto> mono = mock(Mono.class);

        when(carWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/api/v1/cars/" + carId)).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(Class.class))).thenReturn(mono);

        if (responseDto != null) {
            when(mono.block()).thenReturn(responseDto);
        } else {
            when(mono.block()).thenThrow(new RuntimeException("Simulated 404 Not Found"));
        }
    }
}