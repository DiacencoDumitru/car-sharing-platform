package com.dynamiccarsharing.dispute.integration;

import com.dynamiccarsharing.dispute.DisputeApplication;
import com.dynamiccarsharing.dispute.integration.client.BookingIntegrationClient;
import com.dynamiccarsharing.dispute.integration.client.UserIntegrationClient;
import com.dynamiccarsharing.util.exception.ServiceException;
import com.dynamiccarsharing.util.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        classes = {DisputeApplication.class, IntegrationClientsIntegrationTest.MockWebClientConfig.class},
        properties = {
                "application.http.clients.timeout-seconds=1",
                "application.http.clients.retry-max-attempts=0"
        }
)
@ActiveProfiles({"test", "jpa"})
class IntegrationClientsIntegrationTest {

    @Autowired
    private UserIntegrationClient userIntegrationClient;

    @Autowired
    private BookingIntegrationClient bookingIntegrationClient;

    @TestConfiguration
    static class MockWebClientConfig {
        @Bean
        @Primary
        WebClient.Builder webClientBuilder() {
            return WebClient.builder().exchangeFunction(selectiveExchangeFunction());
        }

        private ExchangeFunction selectiveExchangeFunction() {
            return request -> {
                String path = request.url().getPath();
                if (path.contains("/users/404")) {
                    return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
                }
                if (path.contains("/bookings/404")) {
                    return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
                }
                if (path.contains("/users/500") || path.contains("/bookings/500")) {
                    return Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build());
                }
                return Mono.just(ClientResponse.create(HttpStatus.OK).body("{\"id\":1}").build());
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
    @DisplayName("BookingIntegrationClient maps 404 to ValidationException")
    void bookingClient_404_mapsToValidation() {
        assertThatThrownBy(() -> bookingIntegrationClient.assertBookingExists(404L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    @DisplayName("UserIntegrationClient maps 5xx to ServiceException")
    void userClient_5xx_mapsToServiceException() {
        assertThatThrownBy(() -> userIntegrationClient.assertUserExists(500L))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    @DisplayName("BookingIntegrationClient maps 5xx to ServiceException")
    void bookingClient_5xx_mapsToServiceException() {
        assertThatThrownBy(() -> bookingIntegrationClient.assertBookingExists(500L))
                .isInstanceOf(ServiceException.class);
    }
}
