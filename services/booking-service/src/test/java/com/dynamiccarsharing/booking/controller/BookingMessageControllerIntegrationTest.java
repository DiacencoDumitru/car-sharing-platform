package com.dynamiccarsharing.booking.controller;

import com.dynamiccarsharing.booking.BookingApplication;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingMessageRepository;
import com.dynamiccarsharing.booking.repository.BookingRepository;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = {BookingApplication.class, BookingMessageControllerIntegrationTest.MockWebClientConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles({"integration", "jpa"})
class BookingMessageControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingMessageRepository bookingMessageRepository;

    @BeforeEach
    void setUp() {
        bookingMessageRepository.deleteAll();
        bookingRepository.findAll().forEach(b -> bookingRepository.deleteById(b.getId()));
    }

    @Test
    @DisplayName("POST and GET messages for renter and owner; outsider gets 403")
    void messaging_happyPathAndForbidden() {
        Booking booking = bookingRepository.save(Booking.builder()
                .renterId(88L)
                .carId(700L)
                .pickupLocationId(1L)
                .startTime(LocalDateTime.now().plusDays(1).withHour(9).withNano(0))
                .endTime(LocalDateTime.now().plusDays(1).withHour(11).withNano(0))
                .status(TransactionStatus.PENDING)
                .build());

        Map<String, String> body = new HashMap<>();
        body.put("body", "Hello from renter");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", "88");
        ResponseEntity<Map> post = restTemplate.postForEntity(
                messagesUrl(booking.getId()),
                new HttpEntity<>(body, headers),
                Map.class
        );
        assertThat(post.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(post.getBody()).isNotNull();
        assertThat(post.getBody().get("body")).isEqualTo("Hello from renter");

        HttpHeaders ownerHeaders = new HttpHeaders();
        ownerHeaders.set("X-User-Id", "99");
        ResponseEntity<List> getOwner = restTemplate.exchange(
                messagesUrl(booking.getId()) + "?afterId=0",
                HttpMethod.GET,
                new HttpEntity<>(ownerHeaders),
                List.class
        );
        assertThat(getOwner.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getOwner.getBody()).hasSize(1);

        HttpHeaders outsiderHeaders = new HttpHeaders();
        outsiderHeaders.setContentType(MediaType.APPLICATION_JSON);
        outsiderHeaders.set("X-User-Id", "777");
        ResponseEntity<Map> forbidden = restTemplate.postForEntity(
                messagesUrl(booking.getId()),
                new HttpEntity<>(Map.of("body", "spam"), outsiderHeaders),
                Map.class
        );
        assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private String messagesUrl(Long bookingId) {
        return "http://localhost:" + port + "/api/v1/bookings/" + bookingId + "/messages";
    }

    @TestConfiguration
    static class MockWebClientConfig {
        @Bean
        @Primary
        WebClient.Builder webClientBuilder() {
            return WebClient.builder().exchangeFunction(request -> {
                String path = request.url().getPath();
                if (path.contains("/cars/700")) {
                    return Mono.just(jsonResponse(HttpStatus.OK,
                            "{\"id\":700,\"status\":\"AVAILABLE\",\"price\":50.00,\"ownerId\":99}"));
                }
                if (path.contains("/cars/")) {
                    return Mono.just(jsonResponse(HttpStatus.OK,
                            "{\"id\":1,\"status\":\"AVAILABLE\",\"price\":50.00,\"ownerId\":1}"));
                }
                if (path.contains("/users/")) {
                    return Mono.just(jsonResponse(HttpStatus.OK, "{\"id\":1}"));
                }
                return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
            });
        }

        private static ClientResponse jsonResponse(HttpStatus status, String json) {
            return ClientResponse.create(status)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(json)
                    .build();
        }
    }
}
