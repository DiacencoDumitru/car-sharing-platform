package com.dynamiccarsharing.booking.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(name = "admin_audit_log")
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "admin_audit_log_seq")
    @SequenceGenerator(name = "admin_audit_log_seq", sequenceName = "admin_audit_log_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 64)
    private AdminAuditAction action;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "actor_user_id")
    private Long actorUserId;
}
