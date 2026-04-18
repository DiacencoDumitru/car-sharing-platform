package com.dynamiccarsharing.booking.repository;

import com.dynamiccarsharing.booking.model.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {

    List<LoyaltyTransaction> findByPaymentId(Long paymentId);
}

