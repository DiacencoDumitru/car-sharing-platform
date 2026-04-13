package com.dynamiccarsharing.gateway.integration;

import com.dynamiccarsharing.gateway.ApiGatewayApplication;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = ApiGatewayApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class ResponseCacheIntegrationTest {

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.2-alpine").withExposedPorts(6379);

    static MockWebServer mockWebServer;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReactiveRedisConnectionFactory redisConnectionFactory;

    @BeforeAll
    static void startMockServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void stopMockServer() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("test.redis.host", REDIS::getHost);
        registry.add("test.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("test.downstream.base-url", () -> mockWebServer.url("/").toString());
    }

    @BeforeEach
    void clearRedis() {
        redisConnectionFactory.getReactiveConnection().serverCommands().flushAll().block(Duration.ofSeconds(3));
    }

    @Test
    void shouldReturnCachedResponseOnSecondGetRequest() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(200)
                .setBody("{\"source\":\"downstream\",\"id\":101}"));

        webTestClient.get()
                .uri("/api/v1/cars/101")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(101);

        webTestClient.get()
                .uri("/api/v1/cars/101")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(101);

        RecordedRequest first = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        RecordedRequest second = mockWebServer.takeRequest(300, TimeUnit.MILLISECONDS);

        assertNotNull(first);
        assertEquals("/api/v1/cars/101", first.getPath());
        assertNull(second);
    }

    @Test
    void shouldInvalidateBookingCacheAfterMutation() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(200)
                .setBody("{\"bookingId\":77,\"status\":\"PENDING\"}"));
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(201)
                .setBody("{\"result\":\"created\"}"));
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(200)
                .setBody("{\"bookingId\":77,\"status\":\"APPROVED\"}"));

        webTestClient.get()
                .uri("/api/v1/bookings/77")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("PENDING");

        webTestClient.post()
                .uri("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"carId\":12}")
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri("/api/v1/bookings/77")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("APPROVED");

        RecordedRequest first = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        RecordedRequest second = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        RecordedRequest third = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        RecordedRequest fourth = mockWebServer.takeRequest(300, TimeUnit.MILLISECONDS);

        assertNotNull(first);
        assertNotNull(second);
        assertNotNull(third);
        assertEquals("GET", first.getMethod());
        assertEquals("POST", second.getMethod());
        assertEquals("GET", third.getMethod());
        assertNull(fourth);
    }

    @Test
    void shouldKeepSeparateCacheForDifferentAuthorizedUsers() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(200)
                .setBody("{\"user\":\"A\"}"));
        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(200)
                .setBody("{\"user\":\"B\"}"));

        webTestClient.get()
                .uri("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-a")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user").isEqualTo("A");

        webTestClient.get()
                .uri("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-b")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user").isEqualTo("B");

        webTestClient.get()
                .uri("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-a")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user").isEqualTo("A");

        webTestClient.get()
                .uri("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-b")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user").isEqualTo("B");

        RecordedRequest first = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        RecordedRequest second = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        RecordedRequest third = mockWebServer.takeRequest(300, TimeUnit.MILLISECONDS);

        assertNotNull(first);
        assertNotNull(second);
        assertNull(third);
    }
}
