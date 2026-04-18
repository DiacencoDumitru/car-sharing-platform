package com.dynamiccarsharing.booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(name = "loyalty_accounts")
public class LoyaltyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "loyalty_account_seq")
    @SequenceGenerator(name = "loyalty_account_seq", sequenceName = "loyalty_account_seq", allocationSize = 1)
    private Long id;

    @NotNull
    @Column(name = "renter_id", nullable = false, unique = true)
    private Long renterId;

    @NotNull
    @PositiveOrZero
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;
}

