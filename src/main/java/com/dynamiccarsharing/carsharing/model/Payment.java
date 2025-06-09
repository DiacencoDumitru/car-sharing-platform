package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import com.dynamiccarsharing.carsharing.util.Validator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.With;

import java.time.LocalDateTime;

@Getter
@ToString
@EqualsAndHashCode
public class Payment {
    private final Long id;
    private final Long bookingId;
    private final double amount;
    @With
    private final TransactionStatus status;
    private final PaymentType paymentMethod;
    private final LocalDateTime createdAt;
    @With
    private final LocalDateTime updatedAt;

    public Payment(Long id, Long bookingId, double amount, TransactionStatus status, PaymentType paymentMethod, LocalDateTime createdAt, LocalDateTime updatedAt) {
        Validator.validateId(id, "ID");
        Validator.validateId(bookingId, "Booking ID");
        Validator.validateNonNull(status, "Status");
        Validator.validateNonNull(paymentMethod, "Payment method");
        Validator.validateNonNull(createdAt, "Created at");
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void validate() {
        if (status == TransactionStatus.COMPLETED && updatedAt == null) {
            throw new IllegalStateException("Updated at must be non-null for COMPLETED status");
        }
    }
}
