package com.dynamiccarsharing.dispute.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import com.dynamiccarsharing.contracts.enums.DisputeStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@EqualsAndHashCode()
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(name = "disputes")
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dispute_seq")
    @SequenceGenerator(name = "dispute_seq", sequenceName = "dispute_seq", allocationSize = 1)
    private Long id;

    @NotNull(message = "Booking ID must not be null.")
    @Column(name = "booking_id", unique = true, nullable = false)
    private Long bookingId;

    @NotNull(message = "Creation User ID must not be null.")
    @Column(name = "creation_user_id", nullable = false)
    private Long creationUserId;

    @NotBlank(message = "Description must be not null.")
    @Column(nullable = false)
    private String description;

    @NotNull(message = "Status must not be null.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisputeStatus status;

    @NotNull(message = "Created at must be not null.")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}