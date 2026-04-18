package com.dynamiccarsharing.booking.pricing;

import java.math.BigDecimal;

public interface PricingService {

    BigDecimal calculateTotalPrice(PricingContext context);

    PriceComponents calculatePriceComponents(PricingContext context);
}

