package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.exception.DisputeNotFoundException;
import com.dynamiccarsharing.carsharing.exception.InvalidDisputeStatusException;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.DisputeRepository;
import com.dynamiccarsharing.carsharing.repository.specification.DisputeSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class DisputeService {

    private final DisputeRepository disputeRepository;

    public DisputeService(DisputeRepository disputeRepository) {
        this.disputeRepository = disputeRepository;
    }

    public Dispute save(Dispute dispute) {
        return disputeRepository.save(dispute);
    }

    public Optional<Dispute> findById(UUID id) {
        return disputeRepository.findById(id);
    }

    public void deleteById(UUID id) {
        if (!disputeRepository.existsById(id)) {
            throw new DisputeNotFoundException("Dispute with ID " + id + " not found.");
        }
        disputeRepository.deleteById(id);
    }

    public List<Dispute> findAll() {
        return disputeRepository.findAll();
    }

    public List<Dispute> findDisputesByStatus(DisputeStatus status) {
        return disputeRepository.findByStatus(status);
    }

    public Optional<Dispute> findDisputeByBookingId(UUID bookingId) {
        return disputeRepository.findByBookingId(bookingId);
    }

    public Dispute resolveDispute(UUID disputeId) {
        Dispute dispute = getDisputeOrThrow(disputeId);
        validateDisputeStatus(dispute.getStatus());

        Dispute updatedDispute = dispute.withStatus(DisputeStatus.RESOLVED)
                .withResolvedAt(LocalDateTime.now());

        return disputeRepository.save(updatedDispute);
    }

    public List<Dispute> searchDisputes(UUID bookingId, DisputeStatus status) {
        Specification<Dispute> spec = Specification
                .where(bookingId != null ? DisputeSpecification.hasBookingId(bookingId) : null)
                .and(status != null ? DisputeSpecification.hasStatus(status) : null);
        return disputeRepository.findAll(spec);
    }

    private Dispute getDisputeOrThrow(UUID disputeId) {
        return disputeRepository.findById(disputeId).orElseThrow(() -> new DisputeNotFoundException("Dispute with ID " + disputeId + " not found"));
    }

    private void validateDisputeStatus(DisputeStatus currentStatus) {
        if (currentStatus != DisputeStatus.OPEN) {
            throw new InvalidDisputeStatusException("Can only resolve an OPEN dispute");
        }
    }
}
