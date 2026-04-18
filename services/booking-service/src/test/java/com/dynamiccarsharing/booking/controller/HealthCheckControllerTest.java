package com.dynamiccarsharing.booking.controller;

import com.dynamiccarsharing.contracts.dto.CarDto;
import com.dynamiccarsharing.contracts.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HealthCheckControllerTest {

    @InjectMocks
    private HealthCheckController controller;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient userWebClient;
    @Mock
    private WebClient carWebClient;

    @Mock
    private WebClient.RequestHeadersUriSpec userRequestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec userRequestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec userResponseSpec;
    @Mock
    private Mono<UserDto> userDtoMono;

    @Mock
    private WebClient.RequestHeadersUriSpec carRequestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec carRequestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec carResponseSpec;
    @Mock
    private Mono<CarDto> carDtoMono;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.baseUrl("lb://user-service")).thenReturn(webClientBuilder);
        when(webClientBuilder.baseUrl("lb://car-service")).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(userWebClient).thenReturn(carWebClient);

        ReflectionTestUtils.setField(controller, "instanceId", "test-instance-123");

        when(userWebClient.get()).thenReturn(userRequestHeadersUriSpec);
        when(userRequestHeadersUriSpec.uri(anyString())).thenReturn(userRequestHeadersSpec);
        when(userRequestHeadersSpec.retrieve()).thenReturn(userResponseSpec);
        when(userResponseSpec.bodyToMono(UserDto.class)).thenReturn(userDtoMono);
        when(userDtoMono.timeout(any(Duration.class))).thenReturn(userDtoMono);

        when(carWebClient.get()).thenReturn(carRequestHeadersUriSpec);
        when(carRequestHeadersUriSpec.uri(anyString())).thenReturn(carRequestHeadersSpec);
        when(carRequestHeadersSpec.retrieve()).thenReturn(carResponseSpec);
        when(carResponseSpec.bodyToMono(CarDto.class)).thenReturn(carDtoMono);
        when(carDtoMono.timeout(any(Duration.class))).thenReturn(carDtoMono);

        controller.init();
    }

    @Test
    void healthCheck_allServicesUp() {
        UserDto mockUser = new UserDto();
        mockUser.setInstanceId("user-instance-1");
        when(userDtoMono.block()).thenReturn(mockUser);

        CarDto mockCar = new CarDto();
        mockCar.setInstanceId("car-instance-1");
        when(carDtoMono.block()).thenReturn(mockCar);

        ResponseEntity<Map<String, Object>> response = controller.healthCheck();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("UP", body.get("bookingService"));
        assertEquals("test-instance-123", body.get("instance"));

        Map<String, Object> connectivity = (Map<String, Object>) body.get("connectivity");
        assertEquals("UP - Responded from instance: user-instance-1", connectivity.get("user-service"));
        assertEquals("UP - Responded from instance: car-instance-1", connectivity.get("car-service"));
    }

    @Test
    void healthCheck_allServicesDown() {
        when(userDtoMono.block()).thenThrow(new RuntimeException("Connection timed out"));
        when(carDtoMono.block()).thenThrow(new RuntimeException("Service unavailable"));

        ResponseEntity<Map<String, Object>> response = controller.healthCheck();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> connectivity = (Map<String, Object>) response.getBody().get("connectivity");
        
        assertEquals("DOWN - Error: Connection timed out", connectivity.get("user-service"));
        assertEquals("DOWN - Error: Service unavailable", connectivity.get("car-service"));
    }

    @Test
    void healthCheck_userReturnsNull() {
        when(userDtoMono.block()).thenReturn(null);
        when(carDtoMono.block()).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = controller.healthCheck();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> connectivity = (Map<String, Object>) response.getBody().get("connectivity");

        assertEquals("UP - Responded from instance: unknown", connectivity.get("user-service"));
        assertEquals("UP - Responded from instance: unknown", connectivity.get("car-service"));
    }

    @Test
    void testUserServiceLoadBalancing_success() {
        Long userId = 10L;
        UserDto mockUser = new UserDto();
        mockUser.setInstanceId("user-instance-5");
        
        when(userDtoMono.block()).thenReturn(mockUser);

        ResponseEntity<Map<String, Object>> response = controller.testUserServiceLoadBalancing(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("test-instance-123", body.get("bookingServiceInstance"));
        assertEquals("user-service", body.get("service"));
        assertEquals(userId, body.get("userId"));
        assertEquals(true, body.get("userFound"));
        assertEquals("user-instance-5", body.get("handledByInstance"));
    }

    @Test
    void testUserServiceLoadBalancing_userReturnsNull() {
        Long userId = 11L;
        when(userDtoMono.block()).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = controller.testUserServiceLoadBalancing(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("userFound"));
        assertEquals("unknown", body.get("handledByInstance"));
    }

    @Test
    void testUserServiceLoadBalancing_callFails() {
        Long userId = 12L;
        when(userDtoMono.block()).thenThrow(new RuntimeException("WebClient error"));

        ResponseEntity<Map<String, Object>> response = controller.testUserServiceLoadBalancing(userId);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(((String) body.get("error")).contains("Load balancing call failed: WebClient error"));
    }
}