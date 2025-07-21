package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.enums.PaymentType;
import com.dynamiccarsharing.carsharing.enums.TransactionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@ToString
@EqualsAndHashCode(exclude = "booking")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(name = "payments")
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_seq")
    @SequenceGenerator(name = "payment_seq", sequenceName = "payment_seq", allocationSize = 1)
    private final Long id;

    @NotNull(message = "Booking must not be null.")
    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private final Booking booking;

    @NotNull(message = "Amount must not be null.")
    @Positive(message = "Amount must be positive.")
    @Column(nullable = false)
    private final BigDecimal amount;

    @With
    @NotNull(message = "Status must be not null.")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private final TransactionStatus status;

    @NotNull(message = "Payment method must be not null.")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private final PaymentType paymentMethod;

    @NotNull(message = "Created at must be not null.")
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @With
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}