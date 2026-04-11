package com.dynamiccarsharing.user.controller;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.UserApplication;
import com.dynamiccarsharing.user.dto.FavoriteCarsResponseDto;
import com.dynamiccarsharing.user.integration.client.CarIntegrationClient;
import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.user.repository.jpa.UserFavoriteCarJpaRepository;
import com.dynamiccarsharing.user.repository.jpa.UserJpaRepository;
import com.dynamiccarsharing.user.service.JwtService;
import com.dynamiccarsharing.util.exception.ValidationException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = {UserApplication.class, FavoriteCarControllerIntegrationTest.MockCarClientConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles({"test", "jpa"})
class FavoriteCarControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private UserFavoriteCarJpaRepository favoriteCarRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clean() {
        favoriteCarRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("PUT then GET favorite cars returns saved car IDs in order")
    void addAndListFavorites() {
        User user = saveUser("renter1@example.com");
        String token = jwtService.generateToken(user);

        putFavorite(token, user.getId(), 100L);
        putFavorite(token, user.getId(), 50L);

        ResponseEntity<FavoriteCarsResponseDto> response = restTemplate.exchange(
                baseUrl(user.getId()),
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)),
                FavoriteCarsResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCarIds()).containsExactly(50L, 100L);
    }

    @Test
    @DisplayName("DELETE removes a favorite; repeated DELETE is idempotent")
    void removeFavorite_idempotent() {
        User user = saveUser("renter2@example.com");
        String token = jwtService.generateToken(user);
        putFavorite(token, user.getId(), 200L);

        ResponseEntity<Void> del1 = restTemplate.exchange(
                baseUrl(user.getId()) + "/200",
                HttpMethod.DELETE,
                new HttpEntity<>(bearerHeaders(token)),
                Void.class
        );
        assertThat(del1.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<FavoriteCarsResponseDto> list = restTemplate.exchange(
                baseUrl(user.getId()),
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)),
                FavoriteCarsResponseDto.class
        );
        assertThat(list.getBody().getCarIds()).isEmpty();

        ResponseEntity<Void> del2 = restTemplate.exchange(
                baseUrl(user.getId()) + "/200",
                HttpMethod.DELETE,
                new HttpEntity<>(bearerHeaders(token)),
                Void.class
        );
        assertThat(del2.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("PUT with unknown car ID returns 400")
    void addFavorite_unknownCar_badRequest() {
        User user = saveUser("renter3@example.com");
        String token = jwtService.generateToken(user);

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl(user.getId()) + "/999",
                HttpMethod.PUT,
                new HttpEntity<>(bearerHeaders(token)),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("JWT for another user cannot access favorites")
    void cannotAccessOtherUserFavorites() {
        User user1 = saveUser("a@example.com");
        User user2 = saveUser("b@example.com");
        String token2 = jwtService.generateToken(user2);

        ResponseEntity<FavoriteCarsResponseDto> response = restTemplate.exchange(
                baseUrl(user1.getId()),
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token2)),
                FavoriteCarsResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private User saveUser(String email) {
        ContactInfo contactInfo = ContactInfo.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .password(passwordEncoder.encode("secret"))
                .phoneNumber("5550000")
                .build();
        User user = User.builder()
                .contactInfo(contactInfo)
                .role(UserRole.RENTER)
                .status(UserStatus.ACTIVE)
                .build();
        return userRepository.save(user);
    }

    private void putFavorite(String token, Long userId, Long carId) {
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl(userId) + "/" + carId,
                HttpMethod.PUT,
                new HttpEntity<>(bearerHeaders(token)),
                Void.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private String baseUrl(Long userId) {
        return "http://localhost:" + port + "/api/v1/users/" + userId + "/favorite-cars";
    }

    private static HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    @TestConfiguration
    public static class MockCarClientConfig {
        @Bean
        @Primary
        CarIntegrationClient carIntegrationClient() {
            return carId -> {
                if (carId == 999L) {
                    throw new ValidationException("Car with ID 999 does not exist or is unavailable.");
                }
            };
        }
    }
}
