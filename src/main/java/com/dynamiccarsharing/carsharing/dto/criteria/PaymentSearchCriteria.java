package com.dynamiccarsharing.carsharing.dto.criteria;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentSearchCriteria {
    private Long bookingId;
    private BigDecimal amount;
    private TransactionStatus status;
    private PaymentType paymentMethod;
}