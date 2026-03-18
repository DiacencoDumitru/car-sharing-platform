package com.dynamiccarsharing.booking.repository;

import com.dynamiccarsharing.booking.model.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {
}

