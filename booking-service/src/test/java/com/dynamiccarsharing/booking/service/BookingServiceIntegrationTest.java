package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.dto.BookingCreateRequestDto;
import com.dynamiccarsharing.booking.dto.PaymentRequestDto;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.BookingApplication;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.service.interfaces.BookingService;
import com.dynamiccarsharing.booking.service.interfaces.PaymentService;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import com.dynamiccarsharing.util.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.client.WebClient.*;

@SpringBootTest(
        classes = {BookingApplication.class, BookingServiceIntegrationTest.MockWebClientConfig.class}
)
@ActiveProfiles({"integration", "jpa"})
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingRepository bookingRepository;

    @TestConfiguration
    static class MockWebClientConfig {
        @Bean
        @Primary
        Builder webClientBuilder() {
            WebClient userWebClient = mockWebClientReturningUser();
            WebClient carWebClient = mockWebClientReturningAvailableCar();
            Builder mockBuilder = mock(Builder.class);
            Builder userBuilder = mock(Builder.class);
            Builder carBuilder = mock(Builder.class);
            when(mockBuilder.baseUrl("lb://user-service")).thenReturn(userBuilder);
            when(mockBuilder.baseUrl("lb://car-service")).thenReturn(carBuilder);
            when(userBuilder.build()).thenReturn(userWebClient);
            when(carBuilder.build()).thenReturn(carWebClient);
            return mockBuilder;
        }

        private static WebClient mockWebClientReturningUser() {
            WebClient wc = mock(WebClient.class);
            var uriSpec = mock(RequestHeadersUriSpec.class);
            ResponseSpec responseSpec = mock(ResponseSpec.class);
            when(wc.get()).thenReturn(uriSpec);
            when(uriSpec.uri(anyString())).thenReturn(uriSpec);
            when(uriSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(UserDto.class)).thenReturn(Mono.just(new UserDto()));
            return wc;
        }

        private static WebClient mockWebClientReturningAvailableCar() {
            WebClient wc = mock(WebClient.class);
            var uriSpec = mock(RequestHeadersUriSpec.class);
            ResponseSpec responseSpec = mock(ResponseSpec.class);
            CarDto car = new CarDto();
            car.setStatus(CarStatus.AVAILABLE);
            when(wc.get()).thenReturn(uriSpec);
            when(uriSpec.uri(anyString())).thenReturn(uriSpec);
            when(uriSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.just(car));
            return wc;
        }
    }

    @Test
    @DisplayName("Second booking overlapping same car and time fails with ValidationException")
    void save_overlappingTimeSameCar_throwsValidationException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        Long carId = 100L;
        Long renterId = 1L;
        Long pickupId = 10L;

        BookingCreateRequestDto first = new BookingCreateRequestDto();
        first.setRenterId(renterId);
        first.setCarId(carId);
        first.setStartTime(start);
        first.setEndTime(end);
        first.setPickupLocationId(pickupId);

        bookingService.save(first);

        BookingCreateRequestDto second = new BookingCreateRequestDto();
        second.setRenterId(renterId);
        second.setCarId(carId);
        second.setStartTime(start.plusHours(2));
        second.setEndTime(end.plusHours(2));
        second.setPickupLocationId(pickupId);

        assertThatThrownBy(() -> bookingService.save(second))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    @DisplayName("Create payment for canceled booking fails with ValidationException")
    void createPayment_canceledBooking_throwsValidationException() {
        Booking booking = savePendingBooking(1L, 200L, 300L);
        bookingService.cancelBooking(booking.getId());

        PaymentRequestDto request = new PaymentRequestDto();
        request.setBookingId(booking.getId());
        request.setAmount(BigDecimal.valueOf(100));
        request.setPaymentMethod(PaymentType.CREDIT_CARD);

        assertThatThrownBy(() -> paymentService.createPayment(booking.getId(), request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("canceled or completed");
    }

    @Test
    @DisplayName("Complete booking without completed payment fails with ValidationException")
    void completeBooking_withoutCompletedPayment_throwsValidationException() {
        Booking booking = savePendingBooking(1L, 200L, 300L);
        bookingService.approveBooking(booking.getId());

        assertThatThrownBy(() -> bookingService.completeBooking(booking.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("completed payment");
    }

    @Test
    @DisplayName("Complete booking with completed payment succeeds")
    void completeBooking_withCompletedPayment_succeeds() {
        Booking booking = savePendingBooking(1L, 200L, 300L);
        bookingService.approveBooking(booking.getId());

        PaymentRequestDto payReq = new PaymentRequestDto();
        payReq.setBookingId(booking.getId());
        payReq.setAmount(BigDecimal.valueOf(50));
        payReq.setPaymentMethod(PaymentType.CREDIT_CARD);
        com.dynamiccarsharing.booking.dto.PaymentDto createdPayment = paymentService.createPayment(booking.getId(), payReq);
        paymentService.confirmPayment(createdPayment.getId());

        var result = bookingService.completeBooking(booking.getId());

        org.assertj.core.api.Assertions.assertThat(result.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
    }

    private Booking savePendingBooking(Long renterId, Long carId, Long pickupLocationId) {
        Booking b = Booking.builder()
                .renterId(renterId)
                .carId(carId)
                .pickupLocationId(pickupLocationId)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .status(TransactionStatus.PENDING)
                .build();
        return bookingRepository.save(b);
    }
}
