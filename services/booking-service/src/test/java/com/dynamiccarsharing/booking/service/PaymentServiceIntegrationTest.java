package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.dto.PaymentDto;
import com.dynamiccarsharing.booking.dto.PaymentRequestDto;
import com.dynamiccarsharing.booking.model.AdminAuditAction;
import com.dynamiccarsharing.booking.integration.client.UserIntegrationClient;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.repository.PaymentRepository;
import com.dynamiccarsharing.booking.repository.ReferralRewardRepository;
import com.dynamiccarsharing.booking.repository.jpa.AdminAuditLogJpaRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles({"integration", "jpa"})
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentServiceImpl paymentService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AdminAuditLogJpaRepository adminAuditLogJpaRepository;

    @Autowired
    private ReferralRewardRepository referralRewardRepository;

    @MockBean
    private UserIntegrationClient userIntegrationClient;

    @BeforeEach
    void setUp() {
        when(userIntegrationClient.findReferredByUserId(anyLong())).thenReturn(Optional.empty());
        adminAuditLogJpaRepository.deleteAll();
        paymentRepository.findAll()
                .forEach(payment -> paymentRepository.deleteById(payment.getId()));
        referralRewardRepository.deleteAll();
        clearBookings();
    }

    @Test
    @DisplayName("createPayment calculates amount via pricing engine")
    void createPayment_calculatesAmountWithPricing() {
        Booking booking = saveBooking();

        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setPaymentMethod(PaymentType.CREDIT_CARD);

        PaymentDto paymentDto = paymentService.createPayment(booking.getId(), requestDto);

        assertThat(paymentDto.getId()).isNotNull();
        assertThat(paymentDto.getAmount()).isGreaterThan(BigDecimal.ZERO);
        assertThat(paymentDto.getStatus()).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    @DisplayName("createPayment ignores client provided bookingId and amount")
    void createPayment_ignoresClientProvidedDerivedFields() {
        Booking booking = saveBooking();

        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setBookingId(booking.getId() + 999);
        requestDto.setAmount(new BigDecimal("0.01"));
        requestDto.setPaymentMethod(PaymentType.CREDIT_CARD);

        PaymentDto paymentDto = paymentService.createPayment(booking.getId(), requestDto);

        assertThat(paymentDto.getBookingId()).isEqualTo(booking.getId());
        assertThat(paymentDto.getAmount()).isGreaterThan(new BigDecimal("0.01"));
    }

    @Test
    @DisplayName("confirmPayment changes status to COMPLETED")
    void confirmPayment_changesStatusToCompleted() {
        Booking booking = saveBooking();

        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setPaymentMethod(PaymentType.CREDIT_CARD);
        PaymentDto created = paymentService.createPayment(booking.getId(), requestDto);

        PaymentDto confirmed = paymentService.confirmPayment(created.getId(), null);

        assertThat(confirmed.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(adminAuditLogJpaRepository.findAll()).hasSize(1);
        assertThat(adminAuditLogJpaRepository.findAll().get(0).getAction()).isEqualTo(AdminAuditAction.PAYMENT_CONFIRM);
        assertThat(adminAuditLogJpaRepository.findAll().get(0).getPaymentId()).isEqualTo(created.getId());
    }

    @Test
    @DisplayName("confirmPayment stores actor user id from audit perspective")
    void confirmPayment_persistsActorUserId() {
        Booking booking = saveBooking();
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setPaymentMethod(PaymentType.CREDIT_CARD);
        PaymentDto created = paymentService.createPayment(booking.getId(), requestDto);

        paymentService.confirmPayment(created.getId(), 42L);

        assertThat(adminAuditLogJpaRepository.findAll().get(0).getActorUserId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("refundPayment writes audit row")
    void refundPayment_writesAuditLog() {
        Booking booking = saveBooking();
        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setPaymentMethod(PaymentType.CREDIT_CARD);
        PaymentDto created = paymentService.createPayment(booking.getId(), requestDto);
        paymentService.confirmPayment(created.getId(), 1L);

        paymentService.refundPayment(created.getId(), 99L);

        assertThat(adminAuditLogJpaRepository.findAll()).hasSize(2);
        assertThat(adminAuditLogJpaRepository.findAll())
                .filteredOn(a -> a.getAction() == AdminAuditAction.PAYMENT_REFUND)
                .singleElement()
                .satisfies(a -> assertThat(a.getActorUserId()).isEqualTo(99L));
    }

    private Booking saveBooking() {
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
                .build();
        return bookingRepository.save(booking);
    }

    private void clearBookings() {
        bookingRepository.findAll()
                .forEach(booking -> bookingRepository.deleteById(booking.getId()));
    }
}

