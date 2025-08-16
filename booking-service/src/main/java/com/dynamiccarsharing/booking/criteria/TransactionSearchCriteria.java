package com.dynamiccarsharing.booking.criteria;

import lombok.Builder;
import lombok.Getter;
import com.dynamiccarsharing.contracts.enums.PaymentType;
import com.dynamiccarsharing.contracts.enums.TransactionStatus;

@Getter
@Builder
public class TransactionSearchCriteria {
    private Long bookingId;
    private TransactionStatus status;
    private PaymentType paymentMethod;
}