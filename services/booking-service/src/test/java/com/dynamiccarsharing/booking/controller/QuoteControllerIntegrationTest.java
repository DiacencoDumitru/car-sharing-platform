package com.dynamiccarsharing.booking.controller;

import com.dynamiccarsharing.booking.BookingApplication;
import com.dynamiccarsharing.booking.dto.PaymentDto;
import com.dynamiccarsharing.booking.dto.PaymentRequestDto;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.LoyaltyAccount;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.repository.LoyaltyAccountRepository;
import com.dynamiccarsharing.booking.repository.PaymentRepository;
import com.dynamiccarsharing.booking.repository.PromoCodeRepository;
import com.dynamiccarsharing.booking.service.interfaces.PaymentService;
import com.dynamiccarsharing.contracts.enums.PaymentType;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest(
        classes = {BookingApplication.class, QuoteControllerIntegrationTest.MockWebClientConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles({"integration", "jpa"})
class QuoteControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PromoCodeRepository promoCodeRepository;

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        promoCodeRepository.deleteAll();
        loyaltyAccountRepository.deleteAll();
        paymentRepository.findAll().forEach(payment -> paymentRepository.deleteById(payment.getId()));
        bookingRepository.findAll().forEach(booking -> bookingRepository.deleteById(booking.getId()));
    }

    @Test
    @DisplayName("POST /api/v1/bookings/quote returns price breakdown without discounts")
    void calculateQuote_basicPriceBreakdown() {
        ResponseEntity<Map> response = restTemplate.postForEntity(url(), requestBody(10L, 200L, null, null), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(number(response.getBody().get("baseAmount"))).isEqualByComparingTo("20.00");
        assertThat(number(response.getBody().get("dynamicMarkupAmount"))).isEqualByComparingTo("0.00");
        assertThat(number(response.getBody().get("discountAmount"))).isEqualByComparingTo("0.00");
        assertThat(number(response.getBody().get("loyaltyAmount"))).isEqualByComparingTo("0.00");
        assertThat(number(response.getBody().get("totalAmount"))).isEqualByComparingTo("20.00");
        assertThat(response.getBody().get("currency")).isEqualTo("USD");
    }

    @Test
    @DisplayName("POST /api/v1/bookings/quote applies promo discount")
    void calculateQuote_withPromoCode() {
        insertPromoCode("PROMO20");

        ResponseEntity<Map> response = restTemplate.postForEntity(url(), requestBody(10L, 201L, "PROMO20", null), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(number(response.getBody().get("discountAmount"))).isEqualByComparingTo("4.00");
        assertThat(number(response.getBody().get("totalAmount"))).isEqualByComparingTo("16.00");
    }

    @Test
    @DisplayName("POST /api/v1/bookings/quote applies loyalty discount without spending points")
    void calculateQuote_withLoyaltyPreview() {
        loyaltyAccountRepository.save(LoyaltyAccount.builder()
                .renterId(77L)
                .balance(new BigDecimal("3.00"))
                .build());

        ResponseEntity<Map> response = restTemplate.postForEntity(url(), requestBody(77L, 202L, null, new BigDecimal("3.00")), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(number(response.getBody().get("loyaltyAmount"))).isEqualByComparingTo("3.00");
        assertThat(number(response.getBody().get("totalAmount"))).isEqualByComparingTo("17.00");
        assertThat(loyaltyAccountRepository.findByRenterId(77L).orElseThrow().getBalance()).isEqualByComparingTo("3.00");
    }

    @Test
    @DisplayName("POST /api/v1/bookings/quote is consistent with payment amount")
    void calculateQuote_matchesPaymentAmount() {
        insertPromoCode("PROMO20");

        ResponseEntity<Map> quoteResponse = restTemplate.postForEntity(url(), requestBody(10L, 303L, "PROMO20", null), Map.class);
        assertThat(quoteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(quoteResponse.getBody()).isNotNull();

        Booking booking = bookingRepository.save(Booking.builder()
                .renterId(10L)
                .carId(303L)
                .pickupLocationId(1L)
                .startTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0))
                .endTime(LocalDateTime.now().plusDays(1).withHour(12).withMinute(0).withSecond(0).withNano(0))
                .promoCode("PROMO20")
                .status(TransactionStatus.PENDING)
                .build());

        PaymentRequestDto paymentRequestDto = new PaymentRequestDto();
        paymentRequestDto.setPaymentMethod(PaymentType.CREDIT_CARD);

        PaymentDto payment = paymentService.createPayment(booking.getId(), paymentRequestDto);
        assertThat(payment.getAmount()).isEqualByComparingTo(number(quoteResponse.getBody().get("totalAmount")));
    }

    @Test
    @DisplayName("POST /api/v1/bookings/quote fails for unavailable car")
    void calculateQuote_unavailableCar() {
        ResponseEntity<Map> response = restTemplate.postForEntity(url(), requestBody(10L, 999L, null, null), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /api/v1/bookings/quote fails when endTime is before startTime")
    void calculateQuote_invalidTimeRange() {
        Map<String, Object> body = quoteBody(10L, 204L, null, null);
        body.put("startTime", LocalDateTime.now().plusDays(2).withNano(0).toString());
        body.put("endTime", LocalDateTime.now().plusDays(1).withNano(0).toString());

        ResponseEntity<Map> response = restTemplate.postForEntity(url(), new HttpEntity<>(body, jsonHeaders()), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /api/v1/bookings/quote with Idempotency-Key header returns 200 when Redis idempotency is disabled")
    void calculateQuote_withIdempotencyKeyHeader_returnsOk() {
        HttpHeaders headers = jsonHeaders();
        headers.set("Idempotency-Key", "quote-itest-key-1");
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(quoteBody(10L, 205L, null, null), headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url(), entity, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(number(response.getBody().get("totalAmount"))).isEqualByComparingTo("20.00");
    }

    private String url() {
        return "http://localhost:" + port + "/api/v1/bookings/quote";
    }

    private HttpEntity<Map<String, Object>> requestBody(Long renterId, Long carId, String promoCode, BigDecimal loyaltyPoints) {
        return new HttpEntity<>(quoteBody(renterId, carId, promoCode, loyaltyPoints), jsonHeaders());
    }

    private Map<String, Object> quoteBody(Long renterId, Long carId, String promoCode, BigDecimal loyaltyPoints) {
        Map<String, Object> body = new HashMap<>();
        body.put("renterId", renterId);
        body.put("carId", carId);
        body.put("startTime", LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0).toString());
        body.put("endTime", LocalDateTime.now().plusDays(1).withHour(12).withMinute(0).withSecond(0).withNano(0).toString());
        body.put("pickupLocationId", 1L);
        if (promoCode != null) {
            body.put("promoCode", promoCode);
        }
        if (loyaltyPoints != null) {
            body.put("loyaltyPointsToUse", loyaltyPoints);
        }
        return body;
    }

    private org.springframework.http.HttpHeaders jsonHeaders() {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private BigDecimal number(Object value) {
        return new BigDecimal(String.valueOf(value));
    }

    private void insertPromoCode(String code) {
        jdbcTemplate.update(
                "insert into promo_codes (id, code, discount_type, discount_value, max_discount, start_at, end_at, active) " +
                        "values (nextval('promo_code_seq'), ?, 'PERCENTAGE', 0.20, 100.00, DATEADD('DAY', -1, CURRENT_TIMESTAMP), DATEADD('DAY', 1, CURRENT_TIMESTAMP), true)",
                code
        );
    }

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
                if (path.contains("/cars/999")) {
                    return Mono.just(jsonResponse(HttpStatus.OK, "{\"id\":999,\"status\":\"RENTED\",\"price\":120.00}"));
                }
                if (path.contains("/cars/")) {
                    return Mono.just(jsonResponse(HttpStatus.OK, "{\"id\":1,\"status\":\"AVAILABLE\",\"price\":120.00}"));
                }
                if (path.contains("/users/")) {
                    return Mono.just(jsonResponse(HttpStatus.OK, "{\"id\":1}"));
                }
                return Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());
            };
        }
    }
}
