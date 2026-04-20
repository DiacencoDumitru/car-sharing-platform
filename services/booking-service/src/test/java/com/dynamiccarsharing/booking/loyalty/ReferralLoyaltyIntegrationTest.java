package com.dynamiccarsharing.booking.loyalty;

import com.dynamiccarsharing.booking.dto.PaymentDto;
import com.dynamiccarsharing.booking.dto.PaymentRequestDto;
import com.dynamiccarsharing.booking.integration.client.UserIntegrationClient;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.repository.LoyaltyAccountRepository;
import com.dynamiccarsharing.booking.repository.LoyaltyTransactionRepository;
import com.dynamiccarsharing.booking.repository.PaymentRepository;
import com.dynamiccarsharing.booking.repository.ReferralRewardRepository;
import com.dynamiccarsharing.booking.service.interfaces.PaymentService;
import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles({"integration", "jpa"})
class ReferralLoyaltyIntegrationTest {

    private static final long REFEREE = 501L;
    private static final long REFERRER = 900L;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Autowired
    private ReferralRewardRepository referralRewardRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private LoyaltyTransactionRepository loyaltyTransactionRepository;

    @MockBean
    private UserIntegrationClient userIntegrationClient;

    @BeforeEach
    void setUp() {
        when(userIntegrationClient.findReferredByUserId(eq(REFEREE))).thenReturn(Optional.of(REFERRER));
        paymentRepository.findAll().forEach(p -> paymentRepository.deleteById(p.getId()));
        loyaltyTransactionRepository.deleteAll();
        referralRewardRepository.deleteAll();
        loyaltyAccountRepository.deleteAll();
        bookingRepository.findAll().forEach(b -> bookingRepository.deleteById(b.getId()));
    }

    @Test
    @DisplayName("First confirmed payment grants fixed referral bonus to referrer once")
    void confirmPayment_grantsReferralBonusOnce() {
        Booking booking = saveBooking(REFEREE, 20);
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setPaymentMethod(PaymentType.CREDIT_CARD);

        PaymentDto payment = paymentService.createPayment(booking.getId(), requestDto);
        paymentService.confirmPayment(payment.getId(), null);

        assertThat(referralRewardRepository.existsByRefereeUserId(REFEREE)).isTrue();
        BigDecimal referrerBalance = loyaltyAccountRepository.findByRenterId(REFERRER).orElseThrow().getBalance();
        assertThat(referrerBalance).isEqualByComparingTo(new BigDecimal("100"));

        Booking secondBooking = saveBooking(REFEREE, 40);
        PaymentDto secondPayment = paymentService.createPayment(secondBooking.getId(), requestDto);
        paymentService.confirmPayment(secondPayment.getId(), null);

        assertThat(referralRewardRepository.findAll()).hasSize(1);
        assertThat(loyaltyAccountRepository.findByRenterId(REFERRER).orElseThrow().getBalance())
                .isEqualByComparingTo(new BigDecimal("100"));
    }

    private Booking saveBooking(long renterId, long dayOffset) {
        LocalDateTime start = LocalDateTime.now()
                .plusDays(dayOffset)
                .withHour(10)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        LocalDateTime end = start.plusHours(2);

        Booking booking = Booking.builder()
                .renterId(renterId)
                .carId(1L)
                .pickupLocationId(1L)
                .startTime(start)
                .endTime(end)
                .status(TransactionStatus.PENDING)
                .build();
        return bookingRepository.save(booking);
    }
}
