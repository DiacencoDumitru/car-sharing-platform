package com.dynamiccarsharing.booking.promo;

import com.dynamiccarsharing.booking.model.PromoCode;
import com.dynamiccarsharing.booking.pricing.PricingContext;
import com.dynamiccarsharing.booking.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromoServiceImpl implements PromoService {

    private final PromoCodeRepository promoCodeRepository;

    @Override
    public BigDecimal calculateDiscount(PricingContext context, BigDecimal priceBeforeDiscount) {
        String promoCodeValue = context.promoCode();
        if (promoCodeValue == null || promoCodeValue.isBlank()) {
            return BigDecimal.ZERO;
        }

        LocalDateTime now = LocalDateTime.now();
        Optional<PromoCode> promoOpt = promoCodeRepository.findByCodeAndActiveIsTrueAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
                promoCodeValue,
                now,
                now
        );

        if (promoOpt.isEmpty()) {
            return BigDecimal.ZERO;
        }

        PromoCode promo = promoOpt.get();
        BigDecimal discount = switch (promo.getDiscountType()) {
            case PERCENTAGE -> priceBeforeDiscount
                    .multiply(promo.getDiscountValue())
                    .setScale(2, RoundingMode.HALF_UP);
            case FIXED_AMOUNT -> promo.getDiscountValue();
        };

        if (promo.getMaxDiscount() != null && discount.compareTo(promo.getMaxDiscount()) > 0) {
            discount = promo.getMaxDiscount();
        }

        if (discount.compareTo(priceBeforeDiscount) > 0) {
            return priceBeforeDiscount;
        }

        return discount.max(BigDecimal.ZERO);
    }
}

