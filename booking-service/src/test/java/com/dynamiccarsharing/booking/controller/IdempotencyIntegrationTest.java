package com.dynamiccarsharing.booking.controller;

import com.dynamiccarsharing.booking.BookingApplication;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.repository.PaymentRepository;
import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.dto.UserDto;
import com.dynamiccarsharing.contracts.enums.CarStatus;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = {BookingApplication.class, IdempotencyIntegrationTest.MockWebClientConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles({"integration", "jpa"})
@Testcontainers(disabledWithoutDocker = true)
class IdempotencyIntegrationTest {

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7.2-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("application.redis.idempotency.enabled", () -> "true");
        registry.add("application.redis.booking-guard.enabled", () -> "false");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void cleanDb() {
        paymentRepository.findAll().forEach(payment -> paymentRepository.deleteById(payment.getId()));
        bookingRepository.findAll().forEach(booking -> bookingRepository.deleteById(booking.getId()));
    }

    @Test
    @DisplayName("POST /bookings with same idempotency key returns same booking")
    void createBooking_sameIdempotencyKey_returnsSameResponse() {
        String url = "http://localhost:" + port + "/api/v1/bookings";
        HttpEntity<Map<String, Object>> request = bookingRequest("idem-booking-1");

        ResponseEntity<Map> first = restTemplate.postForEntity(url, request, Map.class);
        ResponseEntity<Map> second = restTemplate.postForEntity(url, request, Map.class);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(first.getBody()).isNotNull();
        assertThat(second.getBody()).isNotNull();
        assertThat(second.getBody().get("id")).isEqualTo(first.getBody().get("id"));
        assertThat(bookingRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("POST /bookings/{bookingId}/payment with same idempotency key returns same payment")
    void createPayment_sameIdempotencyKey_returnsSameResponse() {
        Booking booking = bookingRepository.save(Booking.builder()
                .renterId(10L)
                .carId(20L)
                .pickupLocationId(30L)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .status(TransactionStatus.PENDING)
                .build());

        String url = "http://localhost:" + port + "/api/v1/bookings/" + booking.getId() + "/payment";
        HttpEntity<Map<String, Object>> request = paymentRequest("idem-payment-1", booking.getId());

        ResponseEntity<Map> first = restTemplate.postForEntity(url, request, Map.class);
        ResponseEntity<Map> second = restTemplate.postForEntity(url, request, Map.class);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(first.getBody()).isNotNull();
        assertThat(second.getBody()).isNotNull();
        assertThat(second.getBody().get("id")).isEqualTo(first.getBody().get("id"));
        assertThat(paymentRepository.findAll()).hasSize(1);
    }

    private HttpEntity<Map<String, Object>> bookingRequest(String idempotencyKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotency-Key", idempotencyKey);

        Map<String, Object> body = new HashMap<>();
        body.put("renterId", 1);
        body.put("carId", 101);
        body.put("startTime", LocalDateTime.now().plusDays(3).withNano(0).toString());
        body.put("endTime", LocalDateTime.now().plusDays(4).withNano(0).toString());
        body.put("pickupLocationId", 5);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Map<String, Object>> paymentRequest(String idempotencyKey, Long bookingId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotency-Key", idempotencyKey);

        Map<String, Object> body = new HashMap<>();
        body.put("bookingId", bookingId);
        body.put("amount", BigDecimal.valueOf(100));
        body.put("paymentMethod", "CREDIT_CARD");
        return new HttpEntity<>(body, headers);
    }

    @TestConfiguration
    static class MockWebClientConfig {
        @Bean
        @Primary
        WebClient.Builder webClientBuilder() {
            WebClient userWebClient = mockWebClientReturningUser();
            WebClient carWebClient = mockWebClientReturningAvailableCar();
            WebClient.Builder mockBuilder = mock(WebClient.Builder.class);
            WebClient.Builder userBuilder = mock(WebClient.Builder.class);
            WebClient.Builder carBuilder = mock(WebClient.Builder.class);
            when(mockBuilder.baseUrl("lb://user-service")).thenReturn(userBuilder);
            when(mockBuilder.baseUrl("lb://car-service")).thenReturn(carBuilder);
            when(userBuilder.build()).thenReturn(userWebClient);
            when(carBuilder.build()).thenReturn(carWebClient);
            return mockBuilder;
        }

        private static WebClient mockWebClientReturningUser() {
            WebClient wc = mock(WebClient.class);
            var uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
            WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
            when(wc.get()).thenReturn(uriSpec);
            when(uriSpec.uri(anyString())).thenReturn(uriSpec);
            when(uriSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(UserDto.class)).thenReturn(Mono.just(new UserDto()));
            return wc;
        }

        private static WebClient mockWebClientReturningAvailableCar() {
            WebClient wc = mock(WebClient.class);
            var uriSpec = mock(WebClient.RequestHeadersUriSpec.class);
            WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
            CarDto car = new CarDto();
            car.setStatus(CarStatus.AVAILABLE);
            when(wc.get()).thenReturn(uriSpec);
            when(uriSpec.uri(anyString())).thenReturn(uriSpec);
            when(uriSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.just(car));
            return wc;
        }
    }
}
