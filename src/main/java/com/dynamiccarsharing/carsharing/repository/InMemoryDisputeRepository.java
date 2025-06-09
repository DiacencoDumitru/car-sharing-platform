package com.dynamiccarsharing.carsharing.repository;

import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.filter.Filter;

import java.util.*;

public class InMemoryDisputeRepository implements DisputeRepository {
    private final Map<Long, Dispute> disputeMap = new HashMap<>();

    @Override
    public Dispute save(Dispute dispute) {
        disputeMap.put(dispute.getId(), dispute);
        return dispute;
    }

    @Override
    public Optional<Dispute> findById(Long id) {
        return Optional.ofNullable(disputeMap.get(id));
    }

    @Override
    public void deleteById(Long id) {
        disputeMap.remove(id);
    }

    @Override
    public List<Dispute> findByFilter(Filter<Dispute> filter) {
        return disputeMap.values().stream().filter(filter::test).toList();
    }

    @Override
    public Iterable<Dispute> findAll() {
        return disputeMap.values();
    }
}
