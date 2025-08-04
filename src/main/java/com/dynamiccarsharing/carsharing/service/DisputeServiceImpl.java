package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.dto.criteria.DisputeSearchCriteria;
import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.exception.DisputeNotFoundException;
import com.dynamiccarsharing.carsharing.filter.DisputeFilter;
import com.dynamiccarsharing.carsharing.filter.Filter;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.DisputeRepository;
import com.dynamiccarsharing.carsharing.service.interfaces.DisputeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service("disputeService")
@Transactional
public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputeRepository;

    public DisputeServiceImpl(DisputeRepository disputeRepository) {
        this.disputeRepository = disputeRepository;
    }

    @Override
    public Dispute save(Dispute dispute) {
        return disputeRepository.save(dispute);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dispute> findById(Long id) {
        return disputeRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<Dispute> findAll() {
        return disputeRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        if (disputeRepository.findById(id).isEmpty()) {
            throw new DisputeNotFoundException("Dispute with ID " + id + " not found.");
        }
        disputeRepository.deleteById(id);
    }

    @Override
    public Dispute resolveDispute(Long disputeId) {
        Dispute dispute = getDisputeOrThrow(disputeId);
        if (dispute.getStatus() != DisputeStatus.OPEN) {
            throw new IllegalStateException("Can only resolve an OPEN dispute.");
        }
        return disputeRepository.save(dispute.withStatus(DisputeStatus.RESOLVED));
    }

    @Override
    public Dispute updateDisputeDescription(Long disputeId, String newDescription) {
        Dispute dispute = getDisputeOrThrow(disputeId);
        return disputeRepository.save(dispute.withDescription(newDescription));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dispute> searchDisputes(DisputeSearchCriteria criteria) {
        Filter<Dispute> filter = DisputeFilter.of(
                criteria.getBookingId(),
                criteria.getStatus()
        );
        try {
            return disputeRepository.findByFilter(filter);
        } catch (SQLException e) {
            throw new RuntimeException("Search for dispute failed", e);
        }
    }

    private Dispute getDisputeOrThrow(Long disputeId) {
        return disputeRepository.findById(disputeId).orElseThrow(() -> new DisputeNotFoundException("Dispute with ID " + disputeId + " not found."));
    }
}