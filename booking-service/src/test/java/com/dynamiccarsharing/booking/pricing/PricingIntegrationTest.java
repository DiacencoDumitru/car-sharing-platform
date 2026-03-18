package com.dynamiccarsharing.booking.pricing;

import com.dynamiccarsharing.booking.model.Booking;
import com.dynamiccarsharing.booking.model.DynamicPricingRule;
import com.dynamiccarsharing.booking.repository.BookingRepository;
import com.dynamiccarsharing.booking.repository.DynamicPricingRuleRepository;
import com.dynamiccarsharing.booking.repository.PaymentRepository;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.dynamiccarsharing.booking.pricing.DynamicPricingRuleType.PICKUP_LOCATION;
import static com.dynamiccarsharing.booking.pricing.DynamicPricingRuleType.TIME_OF_DAY;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"integration", "jpa"})
class PricingIntegrationTest {

    @Autowired
    private PricingService pricingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private DynamicPricingRuleRepository ruleRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        ruleRepository.deleteAll();
        paymentRepository.findAll()
                .forEach(payment -> paymentRepository.deleteById(payment.getId()));
        clearBookings();
    }

    @Test
    @DisplayName("Base price without rules uses duration only")
    void calculatePrice_withoutRules_usesBaseRate() {
        Booking booking = saveBookingAtTime(10, 1L);

        PricingContext context = new PricingContext(
                booking.getId(),
                booking.getRenterId(),
                booking.getCarId(),
                booking.getPickupLocationId(),
                booking.getStartTime(),
                booking.getEndTime(),
                null
        );

        BigDecimal price = pricingService.calculateTotalPrice(context);

        assertThat(price).isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("Peak hours time-of-day rule increases price")
    void calculatePrice_withTimeOfDayRule_appliesMultiplier() {
        Booking booking = saveBookingAtTime(18, 1L);

        DynamicPricingRule rule = DynamicPricingRule.builder()
                .ruleType(TIME_OF_DAY)
                .startHour(17)
                .endHour(20)
                .multiplier(new BigDecimal("1.50"))
                .active(true)
                .build();
        ruleRepository.save(rule);

        PricingContext context = new PricingContext(
                booking.getId(),
                booking.getRenterId(),
                booking.getCarId(),
                booking.getPickupLocationId(),
                booking.getStartTime(),
                booking.getEndTime(),
                null
        );

        BigDecimal price = pricingService.calculateTotalPrice(context);

        assertThat(price).isEqualByComparingTo("30.00");
    }

    @Test
    @DisplayName("Location surcharge is combined with time-of-day rule")
    void calculatePrice_withLocationAndTimeRules_combinesMultipliers() {
        Long pickupLocationId = 5L;
        Booking booking = saveBookingAtTime(18, pickupLocationId);

        DynamicPricingRule peakRule = DynamicPricingRule.builder()
                .ruleType(TIME_OF_DAY)
                .startHour(17)
                .endHour(20)
                .multiplier(new BigDecimal("1.50"))
                .active(true)
                .build();

        DynamicPricingRule locationRule = DynamicPricingRule.builder()
                .ruleType(PICKUP_LOCATION)
                .locationId(pickupLocationId)
                .multiplier(new BigDecimal("1.20"))
                .active(true)
                .build();

        ruleRepository.save(peakRule);
        ruleRepository.save(locationRule);

        PricingContext context = new PricingContext(
                booking.getId(),
                booking.getRenterId(),
                booking.getCarId(),
                booking.getPickupLocationId(),
                booking.getStartTime(),
                booking.getEndTime(),
                null
        );

        BigDecimal price = pricingService.calculateTotalPrice(context);

        assertThat(price).isEqualByComparingTo("36.00");
    }

    private Booking saveBookingAtTime(int startHour, Long pickupLocationId) {
        LocalDateTime start = LocalDateTime.now()
                .withHour(startHour)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        LocalDateTime end = start.plusHours(2);

        Booking booking = Booking.builder()
                .renterId(1L)
                .carId(1L)
                .pickupLocationId(pickupLocationId)
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

