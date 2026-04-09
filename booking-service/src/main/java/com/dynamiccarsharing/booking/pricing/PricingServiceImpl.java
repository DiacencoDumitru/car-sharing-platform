package com.dynamiccarsharing.booking.pricing;

import com.dynamiccarsharing.booking.model.DynamicPricingRule;
import com.dynamiccarsharing.booking.promo.PromoService;
import com.dynamiccarsharing.booking.repository.DynamicPricingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static com.dynamiccarsharing.booking.pricing.DynamicPricingRuleType.PICKUP_LOCATION;
import static com.dynamiccarsharing.booking.pricing.DynamicPricingRuleType.TIME_OF_DAY;

@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private static final BigDecimal DEFAULT_RATE_PER_HOUR = BigDecimal.TEN;

    private final DynamicPricingRuleRepository ruleRepository;
    private final PromoService promoService;

    @Override
    public BigDecimal calculateTotalPrice(PricingContext context) {
        PriceComponents components = calculatePriceComponents(context);
        BigDecimal total = components.total();
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public PriceComponents calculatePriceComponents(PricingContext context) {
        BigDecimal base = calculateBasePrice(context);
        BigDecimal dynamicMarkup = calculateDynamicMarkup(context, base);
        BigDecimal priceBeforeDiscount = base.add(dynamicMarkup);
        BigDecimal discounts = promoService.calculateDiscount(context, priceBeforeDiscount);
        return new PriceComponents(
                base.setScale(2, RoundingMode.HALF_UP),
                dynamicMarkup.setScale(2, RoundingMode.HALF_UP),
                discounts.setScale(2, RoundingMode.HALF_UP)
        );
    }

    private BigDecimal calculateBasePrice(PricingContext context) {
        Duration duration = Duration.between(context.startTime(), context.endTime());
        long minutes = Math.max(duration.toMinutes(), 0);
        BigDecimal hours = BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.CEILING);
        return hours.multiply(resolveHourlyRate(context.carPricePerDay()));
    }

    private BigDecimal resolveHourlyRate(BigDecimal carPricePerDay) {
        if (carPricePerDay == null || carPricePerDay.compareTo(BigDecimal.ZERO) <= 0) {
            return DEFAULT_RATE_PER_HOUR;
        }
        return carPricePerDay
                .divide(BigDecimal.valueOf(24), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDynamicMarkup(PricingContext context, BigDecimal base) {
        BigDecimal multiplier = BigDecimal.ONE;

        List<DynamicPricingRule> timeRules = ruleRepository.findByRuleTypeAndActiveIsTrue(TIME_OF_DAY);
        for (DynamicPricingRule rule : timeRules) {
            if (matchesTimeRule(context.startTime(), rule)) {
                multiplier = multiplier.multiply(rule.getMultiplier());
            }
        }

        if (context.pickupLocationId() != null) {
            List<DynamicPricingRule> locationRules = ruleRepository.findByRuleTypeAndLocationIdAndActiveIsTrue(PICKUP_LOCATION, context.pickupLocationId());
            for (DynamicPricingRule rule : locationRules) {
                multiplier = multiplier.multiply(rule.getMultiplier());
            }
        }

        BigDecimal dynamicTotal = base.multiply(multiplier);
        return dynamicTotal.subtract(base);
    }

    private boolean matchesTimeRule(LocalDateTime startTime, DynamicPricingRule rule) {
        Integer startHour = rule.getStartHour();
        Integer endHour = rule.getEndHour();
        if (startHour == null || endHour == null) {
            return false;
        }
        int hour = startTime.getHour();
        if (startHour <= endHour) {
            return hour >= startHour && hour < endHour;
        }
        return hour >= startHour || hour < endHour;
    }
}

