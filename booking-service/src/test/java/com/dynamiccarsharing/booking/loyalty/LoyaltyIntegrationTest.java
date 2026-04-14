package com.dynamiccarsharing.booking.loyalty;

import com.dynamiccarsharing.booking.dto.PaymentDto;
import com.dynamiccarsharing.booking.dto.PaymentRequestDto;
import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.LoyaltyAccount;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.repository.LoyaltyAccountRepository;
import com.dynamiccarsharing.booking.repository.LoyaltyTransactionRepository;
import com.dynamiccarsharing.booking.repository.PaymentRepository;
import com.dynamiccarsharing.booking.repository.jpa.AdminAuditLogJpaRepository;
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
class LoyaltyIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private LoyaltyAccountRepository loyaltyAccountRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private LoyaltyTransactionRepository loyaltyTransactionRepository;

    @Autowired
    private AdminAuditLogJpaRepository adminAuditLogJpaRepository;

    @BeforeEach
    void setUp() {
        adminAuditLogJpaRepository.deleteAll();
        paymentRepository.findAll()
                .forEach(payment -> paymentRepository.deleteById(payment.getId()));
        loyaltyTransactionRepository.deleteAll();
        loyaltyAccountRepository.deleteAll();
        clearBookings();
    }

    @Test
    @DisplayName("Confirming payment earns loyalty points for renter")
    void confirmPayment_earnsLoyaltyPoints() {
        Booking booking = saveBooking();

        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setPaymentMethod(PaymentType.CREDIT_CARD);

        PaymentDto paymentDto = paymentService.createPayment(booking.getId(), requestDto);
        PaymentDto confirmed = paymentService.confirmPayment(paymentDto.getId(), null);

        assertThat(confirmed.getStatus()).isEqualTo(TransactionStatus.COMPLETED);

        LoyaltyAccount account = loyaltyAccountRepository.findByRenterId(booking.getRenterId()).orElseThrow();
        assertThat(account.getBalance()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Loyalty points can be redeemed to reduce next payment amount")
    void createPayment_withLoyaltyRedemption_reducesAmountAndBalance() {
        Booking firstBooking = saveBooking();

        PaymentRequestDto firstRequest = new PaymentRequestDto();
        firstRequest.setPaymentMethod(PaymentType.CREDIT_CARD);
        PaymentDto firstPayment = paymentService.createPayment(firstBooking.getId(), firstRequest);
        paymentService.confirmPayment(firstPayment.getId(), null);

        LoyaltyAccount account = loyaltyAccountRepository.findByRenterId(firstBooking.getRenterId()).orElseThrow();
        BigDecimal initialBalance = account.getBalance();
        assertThat(initialBalance).isGreaterThan(BigDecimal.ZERO);

        Booking secondBooking = saveBooking();

        PaymentRequestDto secondRequest = new PaymentRequestDto();
        secondRequest.setPaymentMethod(PaymentType.CREDIT_CARD);
        secondRequest.setLoyaltyPointsToUse(initialBalance);

        PaymentDto secondPayment = paymentService.createPayment(secondBooking.getId(), secondRequest);

        assertThat(secondPayment.getAmount()).isLessThan(firstPayment.getAmount());

        LoyaltyAccount updatedAccount = loyaltyAccountRepository.findByRenterId(firstBooking.getRenterId()).orElseThrow();
        assertThat(updatedAccount.getBalance()).isLessThan(initialBalance);
    }

    @Test
    @DisplayName("Refund reverses loyalty points earned on confirmed payment")
    void refundPayment_reversesEarnOnEarnOnlyPayment() {
        Booking booking = saveBooking();

        PaymentRequestDto requestDto = new PaymentRequestDto();
        requestDto.setPaymentMethod(PaymentType.CREDIT_CARD);
        PaymentDto payment = paymentService.createPayment(booking.getId(), requestDto);
        paymentService.confirmPayment(payment.getId(), null);

        BigDecimal afterEarn = loyaltyAccountRepository.findByRenterId(booking.getRenterId()).orElseThrow().getBalance();
        assertThat(afterEarn).isGreaterThan(BigDecimal.ZERO);

        paymentService.refundPayment(payment.getId(), null);

        assertThat(loyaltyAccountRepository.findByRenterId(booking.getRenterId()).orElseThrow().getBalance())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Refund restores balance after redeem and earn on same payment")
    void refundPayment_restoresBalanceAfterRedeemAndEarn() {
        Booking firstBooking = saveBooking();

        PaymentRequestDto firstRequest = new PaymentRequestDto();
        firstRequest.setPaymentMethod(PaymentType.CREDIT_CARD);
        PaymentDto firstPayment = paymentService.createPayment(firstBooking.getId(), firstRequest);
        paymentService.confirmPayment(firstPayment.getId(), null);

        BigDecimal balanceAfterFirst = loyaltyAccountRepository.findByRenterId(firstBooking.getRenterId())
                .orElseThrow()
                .getBalance();

        Booking secondBooking = saveBooking();

        PaymentRequestDto secondRequest = new PaymentRequestDto();
        secondRequest.setPaymentMethod(PaymentType.CREDIT_CARD);
        secondRequest.setLoyaltyPointsToUse(balanceAfterFirst);

        PaymentDto secondPayment = paymentService.createPayment(secondBooking.getId(), secondRequest);
        paymentService.confirmPayment(secondPayment.getId(), null);

        paymentService.refundPayment(secondPayment.getId(), null);

        assertThat(loyaltyAccountRepository.findByRenterId(firstBooking.getRenterId()).orElseThrow().getBalance())
                .isEqualByComparingTo(balanceAfterFirst);
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

