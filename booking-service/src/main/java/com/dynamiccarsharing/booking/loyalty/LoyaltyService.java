package com.dynamiccarsharing.booking.loyalty;

import java.math.BigDecimal;

public interface LoyaltyService {

    BigDecimal redeemPoints(Long renterId, Long paymentId, BigDecimal requestedPoints, BigDecimal initialAmount);

    BigDecimal previewRedeemAmount(Long renterId, BigDecimal requestedPoints, BigDecimal initialAmount);

    void earnPoints(Long renterId, Long paymentId, BigDecimal paidAmount);
}

