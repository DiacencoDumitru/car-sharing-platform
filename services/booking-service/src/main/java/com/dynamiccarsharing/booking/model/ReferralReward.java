package com.dynamiccarsharing.booking.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Entity
@Table(
        name = "referral_rewards",
        uniqueConstraints = @UniqueConstraint(name = "uk_referral_rewards_referee", columnNames = "referee_user_id")
)
public class ReferralReward {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "referral_reward_seq")
    @SequenceGenerator(name = "referral_reward_seq", sequenceName = "referral_reward_seq", allocationSize = 1)
    private Long id;

    @Column(name = "referee_user_id", nullable = false)
    private Long refereeUserId;

    @Column(name = "referrer_user_id", nullable = false)
    private Long referrerUserId;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;
}
