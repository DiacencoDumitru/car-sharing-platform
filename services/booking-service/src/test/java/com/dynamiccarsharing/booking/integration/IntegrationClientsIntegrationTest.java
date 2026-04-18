package com.dynamiccarsharing.booking.integration;

import com.dynamiccarsharing.booking.BookingApplication;
import com.dynamiccarsharing.booking.integration.client.CarIntegrationClient;
import com.dynamiccarsharing.booking.integration.client.UserIntegrationClient;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        classes = {BookingApplication.class, IntegrationClientsIntegrationTest.MockWebClientConfig.class},
        properties = {
                "application.http.clients.timeout-seconds=1",
                "application.http.clients.retry-max-attempts=0"
        }
)
@ActiveProfiles({"integration", "jpa"})
class IntegrationClientsIntegrationTest {

    @Autowired
    private UserIntegrationClient userIntegrationClient;

    @Autowired
    private CarIntegrationClient carIntegrationClient;

    @TestConfiguration
    static class MockWebClientConfig {
        @Bean
        @Primary
        WebClient.Builder webClientBuilder() {
            return WebClient.builder().exchangeFunction(selectiveExchangeFunction());
        }

        private static ClientResponse jsonResponse(HttpStatus status, String json) {
            return ClientResponse.create(status)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(json)
                    .build();
        }

        private ExchangeFunction selectiveExchangeFunction() {
            return request -> {
                String path = request.url().getPath();
                if (path.contains("/users/404")) {
                    return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
                }
                if (path.contains("/cars/404")) {
                    return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
                }
                if (path.contains("/users/500") || path.contains("/cars/500")) {
                    return Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build());
                }
                if (path.contains("/cars/777")) {
                    return Mono.just(jsonResponse(HttpStatus.OK, "{\"id\":777,\"status\":\"RENTED\"}"));
                }
                return Mono.just(jsonResponse(HttpStatus.OK, "{\"id\":1,\"status\":\"AVAILABLE\"}"));
            };
        }
    }

    @Test
    @DisplayName("UserIntegrationClient maps 404 to ValidationException")
    void userClient_404_mapsToValidation() {
        assertThatThrownBy(() -> userIntegrationClient.assertUserExists(404L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    @DisplayName("CarIntegrationClient maps 404 to ValidationException")
    void carClient_404_mapsToValidation() {
        assertThatThrownBy(() -> carIntegrationClient.assertCarAvailable(404L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    @DisplayName("CarIntegrationClient rejects non-available car")
    void carClient_nonAvailable_mapsToValidation() {
        assertThatThrownBy(() -> carIntegrationClient.assertCarAvailable(777L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("not available");
    }

    @Test
    @DisplayName("UserIntegrationClient maps 5xx to ServiceException")
    void userClient_5xx_mapsToServiceException() {
        assertThatThrownBy(() -> userIntegrationClient.assertUserExists(500L))
                .isInstanceOf(ServiceException.class);
    }
}
