package com.dynamiccarsharing.dispute.repository.inmemory;

import com.dynamiccarsharing.contracts.enums.DisputeStatus;
import com.dynamiccarsharing.dispute.model.Dispute;
import com.dynamiccarsharing.dispute.repository.DisputeRepository;
import com.dynamiccarsharing.util.filter.Filter;

import java.util.*;

public class InMemoryDisputeRepositoryJdbcImpl implements DisputeRepository {
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
    public List<Dispute> findAll() {
        return new ArrayList<>(disputeMap.values());
    }

    @Override
    public Optional<Dispute> findByBookingId(Long bookingId) {
        return disputeMap.values().stream()
                .filter(dispute -> dispute.getBookingId().equals(bookingId))
                .findFirst();
    }

    @Override
    public List<Dispute> findByStatus(DisputeStatus status) {
        return disputeMap.values().stream()
                .filter(dispute -> dispute.getStatus() == status)
                .toList();
    }
}