package com.dynamiccarsharing.notification.analytics;

import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "booking_lifecycle_analytics_events",
        uniqueConstraints = @UniqueConstraint(name = "uk_booking_status", columnNames = {"booking_id", "booking_status"})
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingLifecycleAnalyticsEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "renter_id", nullable = false)
    private Long renterId;

    @Column(name = "car_id")
    private Long carId;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false, length = 50)
    private TransactionStatus bookingStatus;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "fraud_risk_score", nullable = false)
    private int fraudRiskScore;

    @Column(name = "attention_required", nullable = false)
    private boolean attentionRequired;

    @Column(name = "notification_sent", nullable = false)
    private boolean notificationSent;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;
}

