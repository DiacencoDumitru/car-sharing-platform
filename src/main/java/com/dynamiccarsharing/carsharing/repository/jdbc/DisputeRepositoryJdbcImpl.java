package com.dynamiccarsharing.carsharing.repository.jdbc;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.model.Dispute;

import java.util.List;
import java.util.Optional;

public interface DisputeRepositoryJdbcImpl extends Repository<Dispute, Long> {
    Optional<Dispute> findByBookingId(Long bookingId);

    List<Dispute> findByStatus(DisputeStatus status);
}