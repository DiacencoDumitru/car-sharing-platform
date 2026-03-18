package com.dynamiccarsharing.booking.repository;

import com.dynamiccarsharing.booking.model.DynamicPricingRule;
import com.dynamiccarsharing.booking.pricing.DynamicPricingRuleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DynamicPricingRuleRepository extends JpaRepository<DynamicPricingRule, Long> {

    List<DynamicPricingRule> findByRuleTypeAndActiveIsTrue(DynamicPricingRuleType ruleType);

    List<DynamicPricingRule> findByRuleTypeAndLocationIdAndActiveIsTrue(DynamicPricingRuleType ruleType, Long locationId);
}

