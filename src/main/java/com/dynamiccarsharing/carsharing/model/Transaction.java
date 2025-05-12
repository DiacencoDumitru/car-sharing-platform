package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.util.Validator;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@ToString
@Getter
public class Transaction {
    private final Long id;
    private final double amount;
    private final TransactionStatus status;
    private final PaymentType paymentMethod;
    private final String transactionId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Transaction(Long id, double amount, TransactionStatus status, PaymentType paymentMethod, String transactionId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        Validator.validateId(id, "ID");
        Validator.validateNonNull(status, "Status");
        Validator.validateNonNull(paymentMethod, "Payment method");
        Validator.validateNonNull(transactionId, "Transaction ID");
        Validator.validateNonNull(createdAt, "Created at");
        if (status == TransactionStatus.COMPLETED) {
            Validator.validateNonNull(status, "Resolved at");
        }
        this.id = id;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
