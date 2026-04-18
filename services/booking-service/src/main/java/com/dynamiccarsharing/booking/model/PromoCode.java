package com.dynamiccarsharing.booking.model;

import com.dynamiccarsharing.booking.promo.DiscountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

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
@Table(name = "promo_codes")
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "promo_code_seq")
    @SequenceGenerator(name = "promo_code_seq", sequenceName = "promo_code_seq", allocationSize = 1)
    private Long id;

    @NotBlank
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    @NotNull
    @PositiveOrZero
    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    @PositiveOrZero
    @Column(name = "max_discount")
    private BigDecimal maxDiscount;

    @FutureOrPresent
    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;
}

