package com.dynamiccarsharing.booking.repository;

import com.dynamiccarsharing.booking.model.ReferralReward;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferralRewardRepository extends JpaRepository<ReferralReward, Long> {

    boolean existsByRefereeUserId(Long refereeUserId);
}
