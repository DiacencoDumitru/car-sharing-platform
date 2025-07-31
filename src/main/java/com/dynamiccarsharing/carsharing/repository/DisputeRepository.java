package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.Dispute;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface DisputeRepository extends Repository<Dispute, Long> {

    Optional<Dispute> findByBookingId(Long bookingId);

    List<Dispute> findByStatus(DisputeStatus status);

    List<Dispute> findByFilter(Filter<Dispute> filter) throws SQLException;
}