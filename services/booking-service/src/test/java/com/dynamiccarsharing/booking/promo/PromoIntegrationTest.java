package com.dynamiccarsharing.booking.promo;

import com.dynamiccarsharing.booking.dto.PaymentDto;
import com.dynamiccarsharing.booking.dto.PaymentRequestDto;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.PromoCode;
import com.dynamiccarsharing.booking.repository.BookingRepository;
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
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"integration", "jpa"})
class PromoIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PromoCodeRepository promoCodeRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        promoCodeRepository.deleteAll();
        paymentRepository.findAll()
                .forEach(payment -> paymentRepository.deleteById(payment.getId()));
        clearBookings();
    }

    @Test
    @DisplayName("Valid promo code applies configured percentage discount")
    void createPayment_withValidPercentagePromo_appliesDiscount() {
        Booking booking = saveBookingWithPromo("PROMO10");

        PromoCode promoCode = PromoCode.builder()
                .code("PROMO10")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("0.10"))
                .active(true)
                .endAt(LocalDateTime.now().plusDays(1))
                .build();
        promoCodeRepository.save(promoCode);

        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setPaymentMethod(PaymentType.CREDIT_CARD);

        PaymentDto paymentDto = paymentService.createPayment(booking.getId(), requestDto);

        assertThat(paymentDto.getAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("Expired promo code is ignored and price stays unchanged")
    void createPayment_withExpiredPromo_doesNotApplyDiscount() {
        Booking booking = saveBookingWithPromo("PROMO10");

        PromoCode expiredPromo = PromoCode.builder()
                .code("PROMO10")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("0.10"))
                .active(true)
                .endAt(LocalDateTime.now().minusDays(1))
                .build();
        promoCodeRepository.save(expiredPromo);

        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setPaymentMethod(PaymentType.CREDIT_CARD);

        PaymentDto paymentDto = paymentService.createPayment(booking.getId(), requestDto);

        assertThat(paymentDto.getAmount()).isEqualByComparingTo("20.00");
    }

    private Booking saveBookingWithPromo(String promoCode) {
        LocalDateTime start = LocalDateTime.now()
                .withHour(10)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        LocalDateTime end = start.plusHours(2);

        Booking booking = Booking.builder()
                .renterId(1L)
                .carId(1L)
                .pickupLocationId(1L)
                .startTime(start)
                .endTime(end)
                .status(TransactionStatus.PENDING)
                .promoCode(promoCode)
                .build();
        return bookingRepository.save(booking);
    }

    private void clearBookings() {
        bookingRepository.findAll()
                .forEach(booking -> bookingRepository.deleteById(booking.getId()));
    }
}

