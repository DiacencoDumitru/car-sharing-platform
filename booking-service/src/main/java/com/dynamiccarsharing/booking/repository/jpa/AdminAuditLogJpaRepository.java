package com.dynamiccarsharing.booking.repository.jpa;

import com.dynamiccarsharing.booking.model.AdminAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAuditLogJpaRepository extends JpaRepository<AdminAuditLog, Long> {
}
