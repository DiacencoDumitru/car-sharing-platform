package com.dynamiccarsharing.booking.pricing;

import java.math.BigDecimal;

public record PriceComponents(
        BigDecimal basePrice,
        BigDecimal dynamicMarkup,
        BigDecimal discounts
) {

    public BigDecimal total() {
        return basePrice
                .add(dynamicMarkup)
                .subtract(discounts);
    }
}

