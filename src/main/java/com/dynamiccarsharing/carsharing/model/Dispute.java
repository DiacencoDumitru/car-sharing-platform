package com.dynamiccarsharing.carsharing.model;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@ToString
@EqualsAndHashCode(exclude = {"booking", "creationUser"})
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(name = "disputes")
public class Dispute {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dispute_seq")
    @SequenceGenerator(name = "dispute_seq", sequenceName = "dispute_seq", allocationSize = 1)
    private final Long id;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private final Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creation_user_id", nullable = false)
    private final User creationUser;

    @With
    @NotBlank(message = "Description must be not null.")
    @Column(nullable = false)
    private final String description;

    @With
    @NotNull(message = "Status must not be null.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private final DisputeStatus status;

    @NotNull(message = "Created at must be not null.")
    @Column(name = "created_at", nullable = false)
    private final LocalDateTime createdAt;

    @With
    @Column(name = "resolved_at")
    private final LocalDateTime resolvedAt;
}