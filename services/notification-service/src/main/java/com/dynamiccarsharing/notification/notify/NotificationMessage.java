package com.dynamiccarsharing.notification.notify;

import com.dynamiccarsharing.contracts.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "notification_messages",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_booking_status_channel",
                columnNames = {"booking_id", "booking_status", "channel"}
        )
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "renter_id", nullable = false)
    private Long renterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false, length = 50)
    private TransactionStatus bookingStatus;

    @Column(name = "channel", nullable = false, length = 50)
    private String channel;

    @Column(name = "recipient", nullable = false, length = 255)
    private String recipient;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;
}

