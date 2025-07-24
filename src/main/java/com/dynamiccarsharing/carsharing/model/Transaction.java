package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@ToString
@EqualsAndHashCode(exclude = "booking")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(name = "transactions")
@EntityListeners(AuditingEntityListener.class)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_seq")
    @SequenceGenerator(name = "transaction_seq", sequenceName = "transaction_seq", allocationSize = 1)
    private Long id;

    @NotNull(message = "Booking must not be null.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @NotNull(message = "Amount must not be null.")
    @Positive(message = "Amount must be positive.")
    @Column(nullable = false)
    private BigDecimal amount;

    @With
    @NotNull(message = "Status must not be null.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @NotNull(message = "Payment method must not be null.")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentType paymentMethod;

    @NotNull(message = "Creation time must not be null.")
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @With
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}