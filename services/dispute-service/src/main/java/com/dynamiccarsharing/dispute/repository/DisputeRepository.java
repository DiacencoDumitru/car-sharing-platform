package com.dynamiccarsharing.dispute.repository;


import com.dynamiccarsharing.contracts.enums.DisputeStatus;
import com.dynamiccarsharing.dispute.model.Dispute;
import com.dynamiccarsharing.util.filter.Filter;
import com.dynamiccarsharing.util.repository.Repository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface DisputeRepository extends Repository<Dispute, Long> {

    Optional<Dispute> findByBookingId(Long bookingId);

    List<Dispute> findByStatus(DisputeStatus status);

    List<Dispute> findByFilter(Filter<Dispute> filter) throws SQLException;
}