package com.dynamiccarsharing.user.controller;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.UserApplication;
import com.dynamiccarsharing.user.dto.me.BookingPageResponse;
import com.dynamiccarsharing.user.dto.me.TransactionPageResponse;
import com.dynamiccarsharing.user.integration.client.BookingMeClient;
import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.user.repository.jpa.UserJpaRepository;
import com.dynamiccarsharing.user.service.JwtService;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = {UserApplication.class, MeDataControllerIntegrationTest.MockBookingMeClientConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles({"test", "jpa"})
class MeDataControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;
    private Long userId;

    @BeforeEach
    void setUp() {
        MockBookingMeClientConfig.LAST_AS_ROLE.set(null);
        MockBookingMeClientConfig.LAST_USER_ID.set(null);
        userRepository.deleteAll();

        ContactInfo contact = ContactInfo.builder()
                .email("me-data@example.com")
                .phoneNumber("+10000000002")
                .firstName("Me")
                .lastName("Data")
                .password(passwordEncoder.encode("secret"))
                .build();

        User user = User.builder()
                .contactInfo(contact)
                .role(UserRole.RENTER)
                .status(UserStatus.ACTIVE)
                .build();

        userId = userRepository.save(user).getId();
        token = jwtService.generateToken(userRepository.findById(userId).orElseThrow());
    }

    @Test
    @DisplayName("GET /users/me/bookings without token returns 403")
    void bookings_withoutAuth_returnsForbidden() {
        ResponseEntity<BookingPageResponse> response = restTemplate.exchange(
                base("/users/me/bookings"),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("GET /users/me/bookings with JWT delegates to booking client")
    void bookings_withAuth_returnsPage() {
        ResponseEntity<BookingPageResponse> response = restTemplate.exchange(
                base("/users/me/bookings") + "?asRole=owner",
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)),
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
        assertThat(response.getBody().getTotalElements()).isZero();
        assertThat(MockBookingMeClientConfig.LAST_AS_ROLE.get()).isEqualTo("owner");
        assertThat(MockBookingMeClientConfig.LAST_USER_ID.get()).isEqualTo(userId);
    }

    @Test
    @DisplayName("GET /users/me/transactions with JWT returns page from booking client")
    void transactions_withAuth_returnsPage() {
        ResponseEntity<TransactionPageResponse> response = restTemplate.exchange(
                base("/users/me/transactions"),
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)),
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
        assertThat(response.getBody().getTotalElements()).isZero();
    }

    private String base(String path) {
        return "http://localhost:" + port + "/api/v1" + path;
    }

    private static HttpHeaders bearerHeaders(String jwt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwt);
        return headers;
    }

    @TestConfiguration
    static class MockBookingMeClientConfig {

        static final AtomicReference<String> LAST_AS_ROLE = new AtomicReference<>();
        static final AtomicReference<Long> LAST_USER_ID = new AtomicReference<>();

        @Bean
        @Primary
        BookingMeClient bookingMeClient() {
            return new BookingMeClient() {
                @Override
                public BookingPageResponse getUserBookings(Long uid, String asRole, Pageable pageable) {
                    LAST_USER_ID.set(uid);
                    LAST_AS_ROLE.set(asRole);
                    BookingPageResponse r = new BookingPageResponse();
                    r.setContent(List.of());
                    r.setTotalPages(0);
                    r.setTotalElements(0);
                    return r;
                }

                @Override
                public TransactionPageResponse getUserTransactions(Long uid, Pageable pageable) {
                    TransactionPageResponse r = new TransactionPageResponse();
                    r.setContent(List.of());
                    r.setTotalPages(0);
                    r.setTotalElements(0);
                    return r;
                }
            };
        }
    }
}
