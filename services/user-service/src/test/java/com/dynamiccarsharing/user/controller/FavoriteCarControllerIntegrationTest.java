package com.dynamiccarsharing.user.controller;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.UserApplication;
import com.dynamiccarsharing.user.dto.FavoriteCarsResponseDto;
import com.dynamiccarsharing.user.integration.client.CarIntegrationClient;
import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.user.repository.jpa.FavoriteCarJpaRepository;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

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
    private FavoriteCarJpaRepository favoriteCarJpaRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String meBaseUrl;
    private String meToken;
    private Long meUserId;

    @BeforeEach
    void clean() {
        favoriteCarJpaRepository.deleteAll();
        userRepository.deleteAll();

        ContactInfo contact = ContactInfo.builder()
                .email("fav-me@example.com")
                .phoneNumber("+10000000001")
                .firstName("Fav")
                .lastName("User")
                .password(passwordEncoder.encode("secret"))
                .build();

        User user = User.builder()
                .contactInfo(contact)
                .role(UserRole.RENTER)
                .status(UserStatus.ACTIVE)
                .build();

        meUserId = userRepository.save(user).getId();
        meToken = jwtService.generateToken(userRepository.findById(meUserId).orElseThrow());
        meBaseUrl = "http://localhost:" + port + "/api/v1/users/me/favorite-cars";
    }

    @Test
    @DisplayName("GET /users/me/favorite-cars without token returns 403")
    void listFavorites_withoutAuth_returnsForbidden() {
        ResponseEntity<List<Long>> response = restTemplate.exchange(
                meBaseUrl,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("GET/PUT/DELETE /users/me/favorite-cars round-trip with recent-first ordering")
    void listAddRemove_me_roundTrip() {
        HttpHeaders headers = bearerHeaders(meToken);

        ResponseEntity<List<Long>> empty = restTemplate.exchange(
                meBaseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(empty.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(empty.getBody()).isEmpty();

        assertThat(restTemplate.exchange(
                meBaseUrl + "/303",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                Void.class
        ).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(restTemplate.exchange(
                meBaseUrl + "/303",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                Void.class
        ).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<List<Long>> withOne = restTemplate.exchange(
                meBaseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(withOne.getBody()).containsExactly(303L);

        restTemplate.exchange(
                meBaseUrl + "/404",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                Void.class
        );

        ResponseEntity<List<Long>> withTwo = restTemplate.exchange(
                meBaseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(withTwo.getBody()).containsExactly(404L, 303L);

        assertThat(restTemplate.exchange(
                meBaseUrl + "/303",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        ).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<List<Long>> afterDelete = restTemplate.exchange(
                meBaseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(afterDelete.getBody()).containsExactly(404L);

        restTemplate.exchange(
                meBaseUrl + "/303",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        ResponseEntity<List<Long>> afterSecondDelete = restTemplate.exchange(
                meBaseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(afterSecondDelete.getBody()).containsExactly(404L);
    }

    @Test
    @DisplayName("PUT then GET /users/{userId}/favorite-cars returns car IDs sorted ascending")
    void addAndListFavorites_userPath() {
        User user = saveUser("renter1@example.com");
        String token = jwtService.generateToken(user);

        putFavoriteUserPath(token, user.getId(), 100L);
        putFavoriteUserPath(token, user.getId(), 50L);

        ResponseEntity<FavoriteCarsResponseDto> response = restTemplate.exchange(
                userPathBase(user.getId()),
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)),
                FavoriteCarsResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCarIds()).containsExactly(50L, 100L);
    }

    @Test
    @DisplayName("DELETE /users/{userId}/favorite-cars/{carId} is idempotent")
    void removeFavorite_userPath_idempotent() {
        User user = saveUser("renter2@example.com");
        String token = jwtService.generateToken(user);
        putFavoriteUserPath(token, user.getId(), 200L);

        assertThat(restTemplate.exchange(
                userPathBase(user.getId()) + "/200",
                HttpMethod.DELETE,
                new HttpEntity<>(bearerHeaders(token)),
                Void.class
        ).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<FavoriteCarsResponseDto> list = restTemplate.exchange(
                userPathBase(user.getId()),
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token)),
                FavoriteCarsResponseDto.class
        );
        assertThat(list.getBody().getCarIds()).isEmpty();

        assertThat(restTemplate.exchange(
                userPathBase(user.getId()) + "/200",
                HttpMethod.DELETE,
                new HttpEntity<>(bearerHeaders(token)),
                Void.class
        ).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("PUT /users/{userId}/favorite-cars with unknown car returns 400")
    void addFavorite_userPath_unknownCar_badRequest() {
        User user = saveUser("renter3@example.com");
        String token = jwtService.generateToken(user);

        ResponseEntity<Void> response = restTemplate.exchange(
                userPathBase(user.getId()) + "/999",
                HttpMethod.PUT,
                new HttpEntity<>(bearerHeaders(token)),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("JWT for another user cannot read /users/{userId}/favorite-cars")
    void cannotAccessOtherUserFavorites() {
        User user1 = saveUser("a@example.com");
        User user2 = saveUser("b@example.com");
        String token2 = jwtService.generateToken(user2);

        ResponseEntity<FavoriteCarsResponseDto> response = restTemplate.exchange(
                userPathBase(user1.getId()),
                HttpMethod.GET,
                new HttpEntity<>(bearerHeaders(token2)),
                FavoriteCarsResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("GET /internal/cars/{carId}/favorite-users returns user IDs sorted ascending")
    void listUsersByFavoriteCar_internalPath_returnsSortedUserIds() {
        User user1 = saveUser("fav-a@example.com");
        User user2 = saveUser("fav-b@example.com");
        String user1Token = jwtService.generateToken(user1);
        String user2Token = jwtService.generateToken(user2);

        putFavoriteUserPath(user1Token, user1.getId(), 777L);
        putFavoriteUserPath(user2Token, user2.getId(), 777L);
        putFavoriteUserPath(user2Token, user2.getId(), 888L);

        ResponseEntity<List<Long>> response = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/internal/cars/777/favorite-users",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(user1.getId(), user2.getId());
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

    private void putFavoriteUserPath(String token, Long userId, Long carId) {
        ResponseEntity<Void> response = restTemplate.exchange(
                userPathBase(userId) + "/" + carId,
                HttpMethod.PUT,
                new HttpEntity<>(bearerHeaders(token)),
                Void.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private String userPathBase(Long userId) {
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
