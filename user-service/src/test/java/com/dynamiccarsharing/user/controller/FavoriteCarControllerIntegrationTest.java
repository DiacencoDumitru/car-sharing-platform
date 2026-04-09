package com.dynamiccarsharing.user.controller;

import com.dynamiccarsharing.contracts.enums.UserRole;
import com.dynamiccarsharing.contracts.enums.UserStatus;
import com.dynamiccarsharing.user.model.ContactInfo;
import com.dynamiccarsharing.user.model.User;
import com.dynamiccarsharing.user.repository.jpa.FavoriteCarJpaRepository;
import com.dynamiccarsharing.user.repository.jpa.UserJpaRepository;
import com.dynamiccarsharing.user.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"integration", "jpa"})
class FavoriteCarControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private FavoriteCarJpaRepository favoriteCarJpaRepository;

    @Autowired
    private JwtService jwtService;

    private String baseUrl;
    private String token;
    private Long userId;

    @BeforeEach
    void setUp() {
        favoriteCarJpaRepository.deleteAll();
        userJpaRepository.deleteAll();

        ContactInfo contact = ContactInfo.builder()
                .email("fav-test@example.com")
                .phoneNumber("+10000000001")
                .firstName("Fav")
                .lastName("User")
                .password("encoded")
                .build();

        User user = User.builder()
                .contactInfo(contact)
                .role(UserRole.RENTER)
                .status(UserStatus.ACTIVE)
                .build();

        userId = userJpaRepository.save(user).getId();
        token = jwtService.generateToken(userJpaRepository.findById(userId).orElseThrow());

        baseUrl = "http://localhost:" + port + "/api/v1/users/me/favorite-cars";
    }

    @Test
    @DisplayName("GET favorite-cars without token returns 403")
    void listFavorites_withoutAuth_returnsForbidden() {
        ResponseEntity<List<Long>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("GET favorite-cars returns empty list then cars after PUT")
    void listAddRemove_favoriteCars_roundTrip() {
        HttpHeaders headers = bearerHeaders();

        ResponseEntity<List<Long>> empty = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(empty.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(empty.getBody()).isEmpty();

        ResponseEntity<Void> putFirst = restTemplate.exchange(
                baseUrl + "/303",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                Void.class
        );
        assertThat(putFirst.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Void> putAgain = restTemplate.exchange(
                baseUrl + "/303",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                Void.class
        );
        assertThat(putAgain.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<List<Long>> withOne = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(withOne.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(withOne.getBody()).containsExactly(303L);

        restTemplate.exchange(
                baseUrl + "/404",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                Void.class
        );

        ResponseEntity<List<Long>> withTwo = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(withTwo.getBody()).containsExactly(404L, 303L);

        ResponseEntity<Void> del = restTemplate.exchange(
                baseUrl + "/303",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<List<Long>> afterDelete = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(afterDelete.getBody()).containsExactly(404L);

        restTemplate.exchange(
                baseUrl + "/303",
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        ResponseEntity<List<Long>> afterSecondDelete = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );
        assertThat(afterSecondDelete.getBody()).containsExactly(404L);
    }

    private HttpHeaders bearerHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
