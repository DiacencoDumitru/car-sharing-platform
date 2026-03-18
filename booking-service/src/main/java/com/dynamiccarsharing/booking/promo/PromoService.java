package com.dynamiccarsharing.booking.promo;

import com.dynamiccarsharing.booking.pricing.PricingContext;

import java.math.BigDecimal;

public interface PromoService {

    BigDecimal calculateDiscount(PricingContext context, BigDecimal priceBeforeDiscount);
}

