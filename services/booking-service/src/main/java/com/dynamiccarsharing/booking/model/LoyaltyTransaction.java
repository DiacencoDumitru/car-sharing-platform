package com.dynamiccarsharing.booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(name = "loyalty_transactions")
@EntityListeners(AuditingEntityListener.class)
public class LoyaltyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loyalty_transaction_seq")
    @SequenceGenerator(name = "loyalty_transaction_seq", sequenceName = "loyalty_transaction_seq", allocationSize = 1)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private LoyaltyAccount account;

    @NotNull
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @NotNull
    @Column(name = "earn", nullable = false)
    private Boolean earn;

    @Column(name = "payment_id")
    private Long paymentId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

