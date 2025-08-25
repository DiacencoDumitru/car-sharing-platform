package com.dynamiccarsharing.booking.criteria;

import lombok.Builder;
import lombok.Getter;
import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentSearchCriteria {
    private Long bookingId;
    private BigDecimal amount;
    private TransactionStatus status;
    private PaymentType paymentMethod;
}