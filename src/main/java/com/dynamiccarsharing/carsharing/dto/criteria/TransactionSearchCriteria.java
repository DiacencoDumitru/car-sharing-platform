package com.dynamiccarsharing.carsharing.dto.criteria;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransactionSearchCriteria {
    private Long bookingId;
    private TransactionStatus status;
    private PaymentType paymentMethod;
}