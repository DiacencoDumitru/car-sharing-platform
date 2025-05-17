package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.filter.DisputeFilter;

import java.util.List;

public interface DisputeRepository extends Repository<Dispute, Long> {
    List<Dispute> findByFilter(DisputeFilter filter);
}
