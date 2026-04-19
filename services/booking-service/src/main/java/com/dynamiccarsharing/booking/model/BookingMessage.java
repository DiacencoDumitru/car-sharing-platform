package com.dynamiccarsharing.booking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(name = "booking_messages")
@EntityListeners(AuditingEntityListener.class)
public class BookingMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_message_seq")
    @SequenceGenerator(name = "booking_message_seq", sequenceName = "booking_message_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @NotNull
    @Column(name = "sender_user_id", nullable = false)
    private Long senderUserId;

    @NotNull
    @Column(name = "body", nullable = false, length = 2000)
    private String body;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
