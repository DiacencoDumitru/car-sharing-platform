package com.dynamiccarsharing.notification.fraud;

public record FraudDecision(int fraudRiskScore, boolean attentionRequired) {
}

