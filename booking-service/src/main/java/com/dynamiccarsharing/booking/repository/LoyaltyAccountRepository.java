package com.dynamiccarsharing.booking.repository;

import com.dynamiccarsharing.booking.model.LoyaltyAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Long> {

    Optional<LoyaltyAccount> findByRenterId(Long renterId);
}

