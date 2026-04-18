package com.dynamiccarsharing.booking.model;

import com.dynamiccarsharing.contracts.enums.BookingReminderType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "booking_reminder_sent",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_booking_reminder_type",
                columnNames = {"booking_id", "reminder_type"}
        )
)
public class BookingReminderSent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false, length = 16)
    private BookingReminderType reminderType;
}
