package com.dynamiccarsharing.carsharing.service;

import com.dynamiccarsharing.carsharing.enums.DisputeStatus;
import com.dynamiccarsharing.carsharing.model.Dispute;
import com.dynamiccarsharing.carsharing.repository.DisputeRepository;
import com.dynamiccarsharing.carsharing.repository.filter.DisputeFilter;
import com.dynamiccarsharing.carsharing.util.Validator;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DisputeService {
    private final DisputeRepository disputeRepository;

    public DisputeService(DisputeRepository disputeRepository) {
        this.disputeRepository = disputeRepository;
    }

    public Dispute save(Dispute dispute) {
        Validator.validateNonNull(dispute, "Dispute");
        return disputeRepository.save(dispute);
    }

    public Optional<Dispute> findById(Long id) {
        Validator.validateId(id, "Dispute ID");
        return disputeRepository.findById(id);
    }

    public void deleteById(Long id) {
        Validator.validateId(id, "Dispute ID");
        disputeRepository.deleteById(id);
    }

    public Iterable<Dispute> findAll() {
        return disputeRepository.findAll();
    }

    public List<Dispute> findDisputesByStatus(DisputeStatus status) throws SQLException {
        Validator.validateNonNull(status, "Dispute Status");
        DisputeFilter filter = DisputeFilter.ofStatus(status);
        return disputeRepository.findByFilter(filter);
    }

    public List<Dispute> findDisputesByBookingId(Long bookingId) throws SQLException {
        Validator.validateId(bookingId, "Booking ID");
        DisputeFilter filter = DisputeFilter.ofBookingId(bookingId);
        return disputeRepository.findByFilter(filter);
    }

    public Dispute resolveDispute(Long disputeId) {
        Validator.validateId(disputeId, "Dispute ID");
        Dispute dispute = getDisputeOrThrow(disputeId);
        validateDisputeStatus(dispute.getStatus(), DisputeStatus.OPEN, "Can only resolve an OPEN dispute");
        Dispute updatedDispute = dispute.withStatus(DisputeStatus.RESOLVED).withResolvedAt(LocalDateTime.now());
        updatedDispute.validate();
        return disputeRepository.save(updatedDispute);
    }

    private Dispute getDisputeOrThrow(Long disputeId) {
        return findById(disputeId).orElseThrow(() -> new IllegalArgumentException("Dispute with ID " + disputeId + " not found"));
    }

    private void validateDisputeStatus(DisputeStatus currentStatus, DisputeStatus expectedStatus, String errorMessage) {
        if (currentStatus != expectedStatus) {
            throw new IllegalStateException(errorMessage);
        }
    }
}
