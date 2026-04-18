package com.dynamiccarsharing.booking.service;

import com.dynamiccarsharing.booking.model.AdminAuditAction;
import com.dynamiccarsharing.booking.model.AdminAuditLog;
import com.dynamiccarsharing.booking.repository.jpa.AdminAuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AdminAuditService {

    private final AdminAuditLogJpaRepository adminAuditLogJpaRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void logPaymentAction(Long paymentId, AdminAuditAction action, Long actorUserId) {
        adminAuditLogJpaRepository.save(AdminAuditLog.builder()
                .occurredAt(Instant.now())
                .action(action)
                .paymentId(paymentId)
                .actorUserId(actorUserId)
                .build());
    }
}
