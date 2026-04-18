package com.dynamiccarsharing.notification.reminder;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "booking_reminder_dispatch",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reminder_booking_type",
                columnNames = {"booking_id", "reminder_type"}
        )
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingReminderDispatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "reminder_type", nullable = false, length = 16)
    private String reminderType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
