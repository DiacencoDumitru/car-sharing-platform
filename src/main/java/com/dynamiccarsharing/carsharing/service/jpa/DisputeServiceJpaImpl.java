package com.dynamiccarsharing.carsharing.service.jpa;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.exception.DisputeNotFoundException;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.jpa.DisputeJpaRepository;
import com.dynamiccarsharing.carsharing.specification.DisputeSpecification;
import com.dynamiccarsharing.carsharing.service.interfaces.DisputeService;
import com.dynamiccarsharing.carsharing.dto.DisputeSearchCriteria;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("disputeService")
@Profile("jpa")
@Transactional
public class DisputeServiceJpaImpl implements DisputeService {

    private final DisputeJpaRepository disputeRepository;

    public DisputeServiceJpaImpl(DisputeJpaRepository disputeRepository) {
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
    public void deleteById(Long id) {
        if (!disputeRepository.existsById(id)) {
            throw new DisputeNotFoundException("Dispute with ID " + id + " not found.");
        }
        disputeRepository.deleteById(id);
    }

    @Override
    public Dispute resolveDispute(Long disputeId) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new DisputeNotFoundException("Dispute with ID " + disputeId + " not found."));
        if (dispute.getStatus() != DisputeStatus.OPEN) {
            throw new IllegalStateException("Can only resolve an OPEN dispute.");
        }
        return disputeRepository.save(dispute.withStatus(DisputeStatus.RESOLVED));
    }

    @Override
    public Dispute updateDisputeDescription(Long disputeId, String newDescription) {
        Dispute dispute = disputeRepository.findById(disputeId).orElseThrow(() -> new DisputeNotFoundException("Dispute with ID " + disputeId + " not found."));
        return disputeRepository.save(dispute.withDescription(newDescription));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dispute> searchDisputes(DisputeSearchCriteria criteria) {
        return disputeRepository.findAll(
                DisputeSpecification.withCriteria(
                        criteria.getBookingId(),
                        criteria.getStatus()
                )
        );
    }
}