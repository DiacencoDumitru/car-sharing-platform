package com.dynamiccarsharing.booking.model;

import com.dynamiccarsharing.booking.pricing.DynamicPricingRuleType;
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
@Table(name = "dynamic_pricing_rules")
public class DynamicPricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dynamic_pricing_rule_seq")
    @SequenceGenerator(name = "dynamic_pricing_rule_seq", sequenceName = "dynamic_pricing_rule_seq", allocationSize = 1)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private DynamicPricingRuleType ruleType;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "start_hour")
    private Integer startHour;

    @Column(name = "end_hour")
    private Integer endHour;

    @NotNull
    @PositiveOrZero
    @Column(name = "multiplier", nullable = false)
    private BigDecimal multiplier;

    @NotNull
    @Column(name = "active", nullable = false)
    private Boolean active;
}

